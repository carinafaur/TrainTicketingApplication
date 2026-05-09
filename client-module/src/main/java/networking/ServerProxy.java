package networking;

import domain.Route;
import domain.Schedule;
import domain.Station;
import domain.Train;
import domain.User;
import dtos.AvailableScheduleDTO;
import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import dtos.DTOUtils;
import dtos.JourneySearchDTO;
import dtos.RouteDTO;
import dtos.ScheduleDTO;
import dtos.StationDTO;
import dtos.TrainDTO;
import dtos.UserDTO;
import exceptions.AppException;
import service.IObserver;
import service.IService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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

    private volatile List<ScheduleDTO> lastSchedulesDTO = List.of();

    public ServerProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void setObserver(IObserver clientObserver) {
        this.clientObserver = clientObserver;
    }

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

    @Override
    public List<Station> getAllStations() {
        return fetchList(RequestType.GET_ALL_STATIONS, "stations");
    }

    @Override
    public List<Route> getAllRoutes() {
        return fetchList(RequestType.GET_ALL_ROUTES, "routes");
    }

    @Override
    public void addRoute(Route route) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.ADD_ROUTE, DTOUtils.getDTO(route)));
    }

    @Override
    public void updateRoute(Route newRoute) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.UPDATE_ROUTE, DTOUtils.getDTO(newRoute)));
    }

    @Override
    public void removeRoute(Route route) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.REMOVE_ROUTE, DTOUtils.getDTO(route)));
    }

    @Override
    public Station findStationById(int id) { return null; }

    @Override
    public void addStation(Station station) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.ADD_STATION, DTOUtils.getDTO(station)));
    }

    @Override
    public void updateStation(Station station) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.UPDATE_STATION, DTOUtils.getDTO(station)));
    }

    @Override
    public void removeStation(Station station) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.REMOVE_STATION, DTOUtils.getDTO(station)));
    }

    @Override
    public List<Train> getAllTrains() {
        return fetchList(RequestType.GET_ALL_TRAINS, "trains");
    }

    @Override
    public void addTrain(Train train) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.ADD_TRAIN, DTOUtils.getDTO(train)));
    }

    @Override
    public void updateTrain(Train train) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.UPDATE_TRAIN, DTOUtils.getDTO(train)));
    }

    @Override
    public void removeTrain(Train train) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.REMOVE_TRAIN, DTOUtils.getDTO(train)));
    }

    @Override
    public List<Schedule> getAllSchedules() {
        try {
            checkConnection();
            sendRequest(new Request(RequestType.GET_ALL_SCHEDULES, null));
            Response res = readResponse();
            if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
            @SuppressWarnings("unchecked")
            List<ScheduleDTO> dtos = (List<ScheduleDTO>) res.getData();
            this.lastSchedulesDTO = dtos;
            return new ArrayList<>();
        } catch (AppException e) {
            System.err.println("Error getting schedules: " + e.getMessage());
            this.lastSchedulesDTO = List.of();
            return List.of();
        }
    }

    public List<ScheduleDTO> getAllScheduleDTOs() {
        getAllSchedules();
        return lastSchedulesDTO;
    }

    @Override
    public void addSchedule(Schedule schedule) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.ADD_SCHEDULE, DTOUtils.getDTO(schedule)));
    }

    @Override
    public void updateSchedule(Schedule schedule) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.UPDATE_SCHEDULE, DTOUtils.getDTO(schedule)));
    }

    @Override
    public void removeSchedule(Schedule schedule) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.REMOVE_SCHEDULE, DTOUtils.getDTO(schedule)));
    }

    public void addScheduleDTO(ScheduleDTO dto) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.ADD_SCHEDULE, dto));
    }

    public void updateScheduleDTO(ScheduleDTO dto) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.UPDATE_SCHEDULE, dto));
    }

    public void removeScheduleDTO(ScheduleDTO dto) throws AppException {
        sendRequestExpectingOk(new Request(RequestType.REMOVE_SCHEDULE, dto));
    }

    @Override
    public List<AvailableScheduleDTO> searchAvailableSchedules(JourneySearchDTO criteria) {
        try {
            checkConnection();
            sendRequest(new Request(RequestType.SEARCH_AVAILABLE, criteria));
            Response res = readResponse();
            if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
            @SuppressWarnings("unchecked")
            List<AvailableScheduleDTO> list = (List<AvailableScheduleDTO>) res.getData();
            return list == null ? List.of() : list;
        } catch (AppException e) {
            System.err.println("Search error: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public BookingDTO bookSeats(BookingRequestDTO request) throws AppException {
        sendRequest(new Request(RequestType.BOOK_SEATS, request));
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
        return (BookingDTO) res.getData();
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        return fetchList(RequestType.GET_ALL_BOOKINGS, "bookings");
    }

    @Override
    public List<BookingDTO> getBookingsForUser(String username) {
        try {
            checkConnection();
            sendRequest(new Request(RequestType.GET_MY_BOOKINGS, username));
            Response res = readResponse();
            if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
            @SuppressWarnings("unchecked")
            List<BookingDTO> list = (List<BookingDTO>) res.getData();
            return list == null ? List.of() : list;
        } catch (AppException e) {
            System.err.println("Error getting my bookings: " + e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> fetchList(RequestType type, String label) {
        try {
            checkConnection();
            sendRequest(new Request(type, null));
            Response res = readResponse();
            if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
            return (List<T>) res.getData();
        } catch (AppException e) {
            System.err.println("Error getting " + label + ": " + e.getMessage());
            return List.of();
        }
    }

    private void sendRequestExpectingOk(Request req) throws AppException {
        sendRequest(req);
        Response res = readResponse();
        if (res.getType() == ResponseType.ERROR) throw new AppException(res.getErrorMessage());
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

    private synchronized void closeConnection() {
        finished = true;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (connection != null) connection.close();
            clientObserver = null;
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
                            case UPDATED:           if (clientObserver != null) clientObserver.routeUpdated((RouteDTO) res.getData()); break;
                            case ADDED:             if (clientObserver != null) clientObserver.routeAdded((RouteDTO) res.getData()); break;
                            case REMOVED:           if (clientObserver != null) clientObserver.routeDeleted((RouteDTO) res.getData()); break;
                            case STATION_ADDED:     if (clientObserver != null) clientObserver.stationAdded((StationDTO) res.getData()); break;
                            case STATION_UPDATED:   if (clientObserver != null) clientObserver.stationUpdated((StationDTO) res.getData()); break;
                            case STATION_REMOVED:   if (clientObserver != null) clientObserver.stationDeleted((StationDTO) res.getData()); break;
                            case TRAIN_ADDED:       if (clientObserver != null) clientObserver.trainAdded((TrainDTO) res.getData()); break;
                            case TRAIN_UPDATED:     if (clientObserver != null) clientObserver.trainUpdated((TrainDTO) res.getData()); break;
                            case TRAIN_REMOVED:     if (clientObserver != null) clientObserver.trainDeleted((TrainDTO) res.getData()); break;
                            case SCHEDULE_ADDED:    if (clientObserver != null) clientObserver.scheduleAdded((ScheduleDTO) res.getData()); break;
                            case SCHEDULE_UPDATED:  if (clientObserver != null) clientObserver.scheduleUpdated((ScheduleDTO) res.getData()); break;
                            case SCHEDULE_REMOVED:  if (clientObserver != null) clientObserver.scheduleDeleted((ScheduleDTO) res.getData()); break;
                            case BOOKING_ADDED:     if (clientObserver != null) clientObserver.bookingAdded((BookingDTO) res.getData()); break;
                            default:                responses.put(res);
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
