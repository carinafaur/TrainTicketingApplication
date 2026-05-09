package dtos;

import domain.Route;
import domain.Schedule;
import domain.ScheduleStop;
import domain.Station;
import domain.Train;
import domain.User;

import java.util.ArrayList;
import java.util.List;

public class DTOUtils {

    public static UserDTO getDTO(User user) {
        return new UserDTO(user.getUsername(), user.getPassword());
    }

    public static RouteDTO getDTO(Route route) {
        return new RouteDTO(
                route.getId(),
                route.getStartStation().getStationName(),
                route.getEndStation().getStationName(),
                route.getStartStation().getId(),
                route.getEndStation().getId());
    }

    public static TrainDTO getDTO(Train train) {
        return new TrainDTO(train.getId(), train.getTrainNumber(), train.getCapacity());
    }

    public static StationDTO getDTO(Station station) {
        return new StationDTO(station.getId(), station.getStationName(), station.getStationCity());
    }

    public static ScheduleStopDTO getDTO(ScheduleStop stop) {
        return new ScheduleStopDTO(
                stop.getId(),
                stop.getStation().getId(),
                stop.getStation().getStationName(),
                stop.getStation().getStationCity(),
                stop.getStopOrder(),
                stop.getArrivalTime(),
                stop.getDepartureTime()
        );
    }

    public static ScheduleDTO getDTO(Schedule s) {
        List<ScheduleStopDTO> stopDTOs = new ArrayList<>();
        if (s.getStops() != null) {
            for (ScheduleStop st : s.getStops()) {
                stopDTOs.add(getDTO(st));
            }
        }

        Route r = s.getRoute();
        Train t = s.getTrain();

        return new ScheduleDTO(
                s.getId(),
                t == null ? 0 : t.getId(),
                t == null ? null : t.getTrainNumber(),
                r == null ? 0 : r.getId(),
                r == null || r.getStartStation() == null ? 0 : r.getStartStation().getId(),
                r == null || r.getStartStation() == null ? null : r.getStartStation().getStationName(),
                r == null || r.getEndStation() == null ? 0 : r.getEndStation().getId(),
                r == null || r.getEndStation() == null ? null : r.getEndStation().getStationName(),
                s.getDepartureTime(),
                s.getArrivalTime(),
                s.getDelayMinutes(),
                s.getStatus(),
                stopDTOs
        );
    }
}
