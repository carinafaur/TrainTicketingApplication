package networking;

import domain.Route;
import domain.Station;
import domain.Train;
import domain.User;
import dtos.DTOUtils;
import dtos.RouteDTO;
import dtos.TrainDTO;
import dtos.UserDTO;
import exceptions.AppException;
import service.IObserver;
import service.IService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ServerProxy implements IService {
    private final String host;
    private final int port;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket connection;

    private volatile IObserver clientObserver;
    private volatile boolean finished;
    private final BlockingQueue<Response> responses = new LinkedBlockingDeque<>();

    public ServerProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void setObserver(IObserver clientObserver) {
        this.clientObserver = clientObserver;
    }

    // ---------------------------------------------------------------- Auth

    @Override
    public User loginUser(String username, String password, IObserver client) throws AppException {
        initializeConnection();
        this.clientObserver = client;

        sendRequest(new Request(RequestType.LOGIN, new UserDTO(username, password)));
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) {
            closeConnection();
            throw new AppException(res.getErrorMessage());
        }
        return (User) res.getData();
    }

    @Override
    public void logoutUser(String username, IObserver client) {
        try {
            sendRequest(new Request(RequestType.LOGOUT, new UserDTO(username, null)));
            Response res = readResponse();
            if (res.getType() == ResponseType.ERROR) {
                System.err.println("Logout error: " + res.getErrorMessage());
            }
        } catch (AppException e) {
            System.err.println("Error during logout: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // -------------------------------------------------------------- Routes

    @Override
    public List<Station> getAllStations() {
        try {
            checkConnection();
            sendRequest(new Request(RequestType.GET_ALL_STATIONS, null));
            Response res = readResponse();
            if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
            //noinspection unchecked
            return (List<Station>) res.getData();
        } catch (AppException e) {
            System.err.println("Error getting stations: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Route> getAllRoutes() {
        try {
            checkConnection();
            sendRequest(new Request(RequestType.GET_ALL_ROUTES, null));
            Response res = readResponse();
            if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
            //noinspection unchecked
            return (List<Route>) res.getData();
        } catch (AppException e) {
            System.err.println("Error getting routes: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public void addRoute(Route route) throws AppException {
        sendRequest(new Request(RequestType.ADD_ROUTE, DTOUtils.getDTO(route)));
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
    }

    @Override
    public void updateRoute(Route newRoute) throws AppException {
        sendRequest(new Request(RequestType.UPDATE_ROUTE, DTOUtils.getDTO(newRoute)));
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
    }

    @Override
    public void removeRoute(Route route) throws AppException {
        sendRequest(new Request(RequestType.REMOVE_ROUTE, DTOUtils.getDTO(route)));
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
    }

    @Override
    public Station findStationById(int id) { return null; }

    // -------------------------------------------------------------- Trains

    @Override
    public List<Train> getAllTrains() {
        try {
            checkConnection();
            sendRequest(new Request(RequestType.GET_ALL_TRAINS, null));
            Response res = readResponse();
            if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
            //noinspection unchecked
            return (List<Train>) res.getData();
        } catch (AppException e) {
            System.err.println("Error getting trains: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public void addTrain(Train train) throws AppException {
        sendRequest(new Request(RequestType.ADD_TRAIN, DTOUtils.getDTO(train)));
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
    }

    @Override
    public void updateTrain(Train train) throws AppException {
        sendRequest(new Request(RequestType.UPDATE_TRAIN, DTOUtils.getDTO(train)));
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
    }

    @Override
    public void removeTrain(Train train) throws AppException {
        sendRequest(new Request(RequestType.REMOVE_TRAIN, DTOUtils.getDTO(train)));
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
    }

    // ----------------------------------------------------------- I/O glue

    private void sendRequest(Request request) throws AppException {
        checkConnection();
        try {
            out.writeObject(request);
            out.flush();
        } catch (IOException e) {
            throw new AppException("Error sending object " + e);
        }
    }

    private Response readResponse() throws AppException {
        try {
            return responses.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException("Interrupted while waiting for response");
        }
    }

    private void initializeConnection() throws AppException {
        responses.clear();
        try {
            if (connection == null || connection.isClosed()) {
                connection = new Socket(host, port);
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());

                finished = false;
                Thread t = new Thread(new ReaderThread());
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException e) {
            throw new AppException("Error connecting to server " + e);
        }
    }

    private synchronized void closeConnection() {
        finished = true;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (connection != null) connection.close();
            clientObserver = null;
            System.out.println("Connection and streams closed.");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        } finally {
            in = null;
            out = null;
            connection = null;
        }
    }

    private void checkConnection() throws AppException {
        if (connection == null || connection.isClosed()) {
            throw new AppException("Not connected to server");
        }
    }

    private class ReaderThread implements Runnable {
        public void run() {
            while (!finished) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Response) {
                        Response res = (Response) obj;
                        switch (res.getType()) {
                            case UPDATED:        if (clientObserver != null) clientObserver.routeUpdated((RouteDTO) res.getData()); break;
                            case ADDED:          if (clientObserver != null) clientObserver.routeAdded((RouteDTO) res.getData()); break;
                            case REMOVED:        if (clientObserver != null) clientObserver.routeDeleted((RouteDTO) res.getData()); break;
                            case TRAIN_ADDED:    if (clientObserver != null) clientObserver.trainAdded((TrainDTO) res.getData()); break;
                            case TRAIN_UPDATED:  if (clientObserver != null) clientObserver.trainUpdated((TrainDTO) res.getData()); break;
                            case TRAIN_REMOVED:  if (clientObserver != null) clientObserver.trainDeleted((TrainDTO) res.getData()); break;
                            default:             responses.put(res);
                        }
                    }
                } catch (Exception e) {
                    if (!finished) {
                        System.err.println("Reader error (connection lost): " + e);
                        responses.offer(new Response(ResponseType.ERROR, "Connection lost"));
                        closeConnection();
                        break;
                    }
                }
            }
        }
    }
}
