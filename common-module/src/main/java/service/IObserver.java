package service;

import domain.Route;
import dtos.RouteDTO;

public interface IObserver {
    void routeAdded(RouteDTO newRoute);
    void routeDeleted(RouteDTO oldRoute);
    void routeUpdated(RouteDTO updatedRoute);
}
