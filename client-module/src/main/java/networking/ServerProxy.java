package networking;

import domain.Route;
import domain.Station;
import dtos.DTOUtils;
import dtos.RouteDTO;
import dtos.UserDTO;
import service.IObserver;
import service.IService;
import domain.User;
import exceptions.AppException;

import java.io.*;
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

    @Override
    public List<Station> getAllStations() {
        try {
            checkConnection();

            Request req = new Request(RequestType.GET_ALL_STATIONS, null);
            sendRequest(req);

            Response res = readResponse();

            if (res.getType() == ResponseType.ERROR) {
                throw new AppException(res.getErrorMessage());
            }

            return (List<Station>) res.getData();

        } catch (AppException e) {
            System.err.println("Error getting stations: " + e.getMessage());
            return List.of();
        }
    }

    public ServerProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setObserver(IObserver clientObserver) {
        this.clientObserver = clientObserver;
    }

    @Override
    public User loginUser(String username, String password, IObserver client) throws AppException {
        initializeConnection();
        this.clientObserver = client;

        Request req = new Request(RequestType.LOGIN, new UserDTO(username, password));
        sendRequest(req);

        Response res = readResponse();

        if (res.getType() == ResponseType.ERROR) {
            closeConnection();
            throw new AppException(res.getErrorMessage());
        }
        return (User) res.getData();
    }

    @Override
    public void updateRoute(Route newRoute) throws AppException {
        RouteDTO dto = DTOUtils.getDTO(newRoute);

        Request req = new Request(RequestType.UPDATE_ROUTE, dto);

        sendRequest(req);

        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) {
            closeConnection();
            throw new AppException(res.getErrorMessage());
        }
    }

    @Override
    public void addRoute(Route route) throws AppException {
        RouteDTO dto = DTOUtils.getDTO(route);

        Request req = new Request(RequestType.ADD_ROUTE, dto);

        sendRequest(req);

        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) {
            closeConnection();
            throw new AppException(res.getErrorMessage());
        }
    }

    @Override
    public void removeRoute(Route route) throws AppException {
        RouteDTO dto = DTOUtils.getDTO(route);

        Request req = new Request(RequestType.REMOVE_ROUTE, dto);

        sendRequest(req);

        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) {
            closeConnection();
            throw new AppException(res.getErrorMessage());
        }
    }

    @Override
    public Station findStationById(int id) {
        return null;
    }

    @Override
    public void logoutUser(String username, IObserver client) {
        try {
            Request req = new Request(RequestType.LOGOUT, new UserDTO(username,null));

            sendRequest(req);
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

    @Override
    public List<Route> getAllRoutes() {
        try {
            checkConnection();

            Request req = new Request(RequestType.GET_ALL_ROUTES, null);
            sendRequest(req);

            Response res = readResponse();

            if (res.getType() == ResponseType.ERROR) {
                throw new AppException(res.getErrorMessage());
            }

            return (List<Route>) res.getData();

        } catch (AppException e) {
            System.err.println("Error getting routes: " + e.getMessage());
            return List.of();
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
        }finally {
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
                        if (res.getType() == ResponseType.UPDATED) {
                            handleUpdate(res);
                        } else if(res.getType() == ResponseType.ADDED) {
                            handleAdd(res);
                        }
                        else if(res.getType() == ResponseType.REMOVED) {
                            handleRemove(res);
                        }
                        else {
                            responses.put(res);
                        }
                    }
                } catch (Exception e) {
                    if (!finished) {

                        System.err.println("Reader error (connection lost): " + e);

                        responses.offer(
                                new Response(ResponseType.ERROR, "Connection lost")
                        );

                        closeConnection();
                        break;
                    }
                }
            }
        }
    }

    private void handleUpdate(Response res) {
        clientObserver.routeUpdated((RouteDTO) res.getData());
    }

    private void handleAdd(Response res) {
        clientObserver.routeAdded((RouteDTO) res.getData());
    }

    private void handleRemove(Response res) {
        clientObserver.routeDeleted((RouteDTO) res.getData());
    }


}



