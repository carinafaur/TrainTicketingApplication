package service;

import dtos.BookingDTO;
import dtos.RouteDTO;
import dtos.ScheduleDTO;
import dtos.StationDTO;
import dtos.TrainDTO;

/**
 * Callback contract used by the server to push entity-change notifications
 * to logged-in clients.
 *
 * <p><b>Threading note:</b> implementations are invoked from the
 * server-reader thread on the client side. UI implementations must marshal
 * updates to the UI thread via {@code Platform.runLater(...)}.
 */
public interface IObserver {

    void routeAdded(RouteDTO newRoute);
    void routeDeleted(RouteDTO oldRoute);
    void routeUpdated(RouteDTO updatedRoute);

    void stationAdded(StationDTO newStation);
    void stationDeleted(StationDTO oldStation);
    void stationUpdated(StationDTO updatedStation);

    void trainAdded(TrainDTO newTrain);
    void trainDeleted(TrainDTO oldTrain);
    void trainUpdated(TrainDTO updatedTrain);

    void scheduleAdded(ScheduleDTO newSchedule);
    void scheduleDeleted(ScheduleDTO oldSchedule);
    void scheduleUpdated(ScheduleDTO updatedSchedule);

    void bookingAdded(BookingDTO newBooking);
}
