package service;

import domain.Route;
import exceptions.AppException;

import java.util.List;

/**
 * CRUD operations for {@link Route} entities. Routes are an admin-managed
 * catalog: who manages the corridors between stations.
 */
public interface IRouteService {

    List<Route> getAllRoutes();
    void addRoute(Route route) throws AppException;
    void updateRoute(Route newRoute) throws AppException;
    void removeRoute(Route route) throws AppException;
}
