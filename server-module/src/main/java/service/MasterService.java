package service;

import domain.Route;
import domain.Station;
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
    private Map<String, IObserver> loggedClients = new ConcurrentHashMap<>();

    public MasterService(UserService userService, RouteService routeService, StationService stationService) {
        this.userService = userService;
        this.routeService = routeService;
        this.stationService = stationService;

    }

    @Override
    public User loginUser(String username, String password, IObserver client) throws NotFoundException, AlreadyExistsException {
        User user = userService.loginUser(username, password);

        if (user != null) {
            if (loggedClients.containsKey(user.getId())) {
                throw new AlreadyExistsException("User is already logged in!");
            }
            loggedClients.put(user.getUsername(), client);
        }
        return user;
    }

    @Override
    public void setObserver(IObserver clientObserver) {

    }

    @Override
    public List<Route> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @Override
    public void logoutUser(String username, IObserver client) {
        loggedClients.remove(username);
    }

    @Override
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @Override
    public void addRoute(Route route) throws ValidationException,AlreadyExistsException {
        Route addedRoute = routeService.addRoute(route);
        loggedClients.forEach((key, value) -> {
           value.routeAdded(DTOUtils.getDTO(addedRoute));
        });
    }

    @Override
    public void updateRoute(Route newRoute) throws AppException {
        Route updatedRoute = routeService.updateRoute(newRoute);
        loggedClients.forEach((key, value) -> {
            value.routeUpdated(DTOUtils.getDTO(updatedRoute));
        });
    }

    @Override
    public void removeRoute(Route route) throws AppException {
        routeService.deleteRoute(route);
        loggedClients.forEach((key, value) -> {
            value.routeDeleted(DTOUtils.getDTO(route));
        });
    }

    public Station findStationById(int id) {
        return stationService.findStationById(id);
    }

}
