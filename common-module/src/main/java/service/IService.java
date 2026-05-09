package service;

import domain.Route;
import domain.Station;
import domain.Train;
import domain.User;
import exceptions.AppException;

import java.util.List;

/**
 * Application-level facade for all client operations. Implemented on the
 * server side by {@code MasterService} (which coordinates the lower-level
 * services) and on the client side by {@code ServerProxy} (which forwards
 * each call over the socket).
 *
 * <p>Both implementations are expected to be safe to call from multiple
 * threads. The {@link IObserver} parameter on {@link #loginUser} represents
 * the caller and is later used by the server to push entity-change
 * notifications back to that client.
 */
public interface IService {

    // -------------------------------------------------------------- Auth

    /**
     * Authenticates a user and registers {@code client} as the observer
     * that should receive push notifications for the lifetime of the
     * session.
     *
     * @throws AppException if credentials are invalid, the user is missing,
     *                      or the user is already logged in
     */
    User loginUser(String username, String password, IObserver client) throws AppException;

    /**
     * Detaches {@code client} from the server's observer list. Idempotent:
     * calling it for an unknown username is a no-op.
     */
    void logoutUser(String username, IObserver client);

    /**
     * Sets the observer that should receive push notifications. Used by
     * the client-side proxy to wire its single observer; on the server
     * side the registration happens at login time, so this is a no-op.
     */
    void setObserver(IObserver clientObserver);

    // ------------------------------------------------------------ Routes

    /** Returns all routes currently stored. Never {@code null}. */
    List<Route> getAllRoutes();

    /** Returns all stations currently stored. Never {@code null}. */
    List<Station> getAllStations();

    /**
     * Persists a new route between two stations. Notifies all logged-in
     * clients via {@link IObserver#routeAdded}.
     *
     * @throws AppException if the route is invalid (e.g. missing stations)
     *                      or already exists
     */
    void addRoute(Route route) throws AppException;

    /**
     * Removes the given route. Notifies all logged-in clients via
     * {@link IObserver#routeDeleted}.
     *
     * @throws AppException if the route does not exist
     */
    void removeRoute(Route route) throws AppException;

    /**
     * Updates an existing route. Notifies all logged-in clients via
     * {@link IObserver#routeUpdated}.
     *
     * @throws AppException if the route is invalid, missing, or the new
     *                      start/end pair collides with another route
     */
    void updateRoute(Route newRoute) throws AppException;

    /** Returns the station with the given id, or {@code null} if unknown. */
    Station findStationById(int id);

    // ------------------------------------------------------------ Trains

    /** Returns all trains currently stored. Never {@code null}. */
    List<Train> getAllTrains();

    /**
     * Persists a new train. Train numbers must be unique. Notifies all
     * logged-in clients via {@link IObserver#trainAdded}.
     *
     * @throws AppException on validation errors or duplicate train number
     */
    void addTrain(Train train) throws AppException;

    /**
     * Updates an existing train (number and/or capacity). Notifies all
     * logged-in clients via {@link IObserver#trainUpdated}.
     *
     * @throws AppException if the train is invalid, missing, or the new
     *                      train number collides with another train
     */
    void updateTrain(Train train) throws AppException;

    /**
     * Removes the given train. Notifies all logged-in clients via
     * {@link IObserver#trainDeleted}.
     *
     * @throws AppException if the train does not exist
     */
    void removeTrain(Train train) throws AppException;
}
