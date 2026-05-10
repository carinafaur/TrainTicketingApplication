package service;

import domain.Route;
import exceptions.AlreadyExistsException;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import repository.RouteRepository;
import validators.RouteValidator;

import java.util.List;

public class RouteService {
    private final RouteRepository  routeRepository;
    private final RouteValidator routeValidator;
    public RouteService(RouteRepository routeRepository, RouteValidator routeValidator) {
        this.routeRepository = routeRepository;
        this.routeValidator = routeValidator;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.getAllRoutes();
    }

    public Route addRoute(Route route) throws ValidationException, AlreadyExistsException {
        Route foundRoute=routeRepository.findByStations(route.getStartStation(),route.getEndStation());
        if(foundRoute!=null){
            throw new AlreadyExistsException("Route already exists");
        }else {
            routeValidator.validate(route);
            return routeRepository.save(route);
        }
    }

    public Route updateRoute(Route newRoute) throws ValidationException,NotFoundException, AlreadyExistsException {
        Route foundRouteByStations =routeRepository.findByStations(newRoute.getStartStation(),newRoute.getEndStation());
        Route foundRouteById=routeRepository.findByRouteId(newRoute.getId());
        if(foundRouteByStations !=null){
            throw new AlreadyExistsException("Route does not exist");
        }else if(foundRouteById ==null) {
            throw new NotFoundException("Route does not exist");
        }else{
            routeValidator.validate(newRoute);
            return routeRepository.update(newRoute);
        }
    }

    public Route deleteRoute(Route route) throws NotFoundException {
        Route foundRouteById=routeRepository.findByRouteId(route.getId());
        if(foundRouteById ==null) {
            throw new NotFoundException("Route does not exist");
        }
        return routeRepository.delete(route);
    }

}
