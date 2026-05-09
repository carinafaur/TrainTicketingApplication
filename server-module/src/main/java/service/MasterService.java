package service;

import domain.Route;
import domain.Station;
import domain.Train;
import domain.User;
import dtos.DTOUtils;
import exceptions.AlreadyExistsException;
import exceptions.AppException;
import exceptions.NotFoundException;
import exceptions.ValidationException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MasterService implements IService {
    private final UserService userService;
    private final RouteService routeService;
    private final StationService stationService;
    private final TrainService trainService;
    private final Map<String, IObserver> loggedClients = new ConcurrentHashMap<>();

    public MasterService(UserService userService,
                         RouteService routeService,
                         StationService stationService,
                         TrainService trainService) {
        this.userService = userService;
        this.routeService = routeService;
        this.stationService = stationService;
        this.trainService = trainService;
    }

    @Override
    public User loginUser(String username, String password, IObserver client) throws NotFoundException, AlreadyExistsException {
        User user = userService.loginUser(username, password);
        if (user != null) {
            if (loggedClients.containsKey(user.getUsername())) {
                throw new AlreadyExistsException("User is already logged in!");
            }
            loggedClients.put(user.getUsername(), client);
        }
        return user;
    }

    @Override
    public void setObserver(IObserver clientObserver) {
        // No-op on the server side; client wiring is per-connection in ClientWorker.
    }

    @Override
    public void logoutUser(String username, IObserver client) {
        loggedClients.remove(username);
    }

    @Override
    public List<Route> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @Override
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @Override
    public void addRoute(Route route) throws ValidationException, AlreadyExistsException {
        Route added = routeService.addRoute(route);
        loggedClients.forEach((k, obs) -> obs.routeAdded(DTOUtils.getDTO(added)));
    }

    @Override
    public void updateRoute(Route newRoute) throws AppException {
        Route updated = routeService.updateRoute(newRoute);
        loggedClients.forEach((k, obs) -> obs.routeUpdated(DTOUtils.getDTO(updated)));
    }

    @Override
    public void removeRoute(Route route) throws AppException {
        routeService.deleteRoute(route);
        loggedClients.forEach((k, obs) -> obs.routeDeleted(DTOUtils.getDTO(route)));
    }

    @Override
    public Station findStationById(int id) {
        return stationService.findStationById(id);
    }

    // -------------------------------------------------------------------- Trains

    @Override
    public List<Train> getAllTrains() {
        return trainService.getAllTrains();
    }

    @Override
    public void addTrain(Train train) throws AppException {
        Train added = trainService.addTrain(train);
        loggedClients.forEach((k, obs) -> obs.trainAdded(DTOUtils.getDTO(added)));
    }

    @Override
    public void updateTrain(Train train) throws AppException {
        Train updated = trainService.updateTrain(train);
        loggedClients.forEach((k, obs) -> obs.trainUpdated(DTOUtils.getDTO(updated)));
    }

    @Override
    public void removeTrain(Train train) throws AppException {
        trainService.deleteTrain(train);
        loggedClients.forEach((k, obs) -> obs.trainDeleted(DTOUtils.getDTO(train)));
    }
}
