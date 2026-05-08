package service;

import domain.Route;
import domain.Station;
import domain.User;
import exceptions.AppException;

import java.util.List;

public interface IService {
    public User loginUser(String username, String password, IObserver client) throws AppException;
    public void logoutUser(String username,IObserver client);
    public void setObserver(IObserver clientObserver);
    public List<Route> getAllRoutes();
    public List<Station> getAllStations();
    public void addRoute(Route route) throws AppException;
    public void removeRoute(Route route) throws AppException;
    public void updateRoute(Route newRoute) throws AppException;
    public Station findStationById(int id);

}
