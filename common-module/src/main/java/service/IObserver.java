package service;

import dtos.RouteDTO;
import dtos.TrainDTO;

/**
 * Callback contract used by the server to push entity-change notifications
 * to logged-in clients. Each connected client is registered as an observer
 * at login time and receives a callback for every relevant change made by
 * any other client.
 *
 * <p><b>Threading note:</b> implementations are invoked from the
 * server-reader thread on the client side (see {@code ServerProxy}). UI
 * implementations (JavaFX controllers) must marshal updates to the UI
 * thread, e.g. via {@code Platform.runLater(...)}.
 *
 * <p>DTOs are used (instead of full entities) so that the wire payload
 * stays small and decoupled from the JPA entity graph.
 */
public interface IObserver {

    /** Notifies that a new route was added. */
    void routeAdded(RouteDTO newRoute);

    /** Notifies that an existing route was deleted. */
    void routeDeleted(RouteDTO oldRoute);

    /** Notifies that an existing route was updated (start/end changed). */
    void routeUpdated(RouteDTO updatedRoute);

    /** Notifies that a new train was added. */
    void trainAdded(TrainDTO newTrain);

    /** Notifies that an existing train was deleted. */
    void trainDeleted(TrainDTO oldTrain);

    /** Notifies that an existing train was updated (number or capacity changed). */
    void trainUpdated(TrainDTO updatedTrain);
}
