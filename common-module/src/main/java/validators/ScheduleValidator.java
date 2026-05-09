package validators;

import domain.Schedule;
import domain.ScheduleStop;
import exceptions.ValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScheduleValidator implements IValidator<Schedule> {

    @Override
    public void validate(Schedule schedule) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        if (schedule.getTrain() == null) {
            errors.append("Schedule must reference a train. ");
        }
        if (schedule.getRoute() == null) {
            errors.append("Schedule must reference a route. ");
        }

        List<ScheduleStop> stops = schedule.getStops();
        if (stops == null || stops.size() < 2) {
            throw new ValidationException(errors + "Schedule must have at least 2 stops (start + end).");
        }

        for (int i = 1; i < stops.size(); i++) {
            if (stops.get(i).getStopOrder() <= stops.get(i - 1).getStopOrder()) {
                errors.append("Stops must be in strictly increasing stop_order. ");
                break;
            }
        }

        Set<Integer> stationIds = new HashSet<>();
        for (ScheduleStop st : stops) {
            if (st.getStation() == null) {
                errors.append("Every stop must reference a station. ");
                break;
            }
            if (!stationIds.add(st.getStation().getId())) {
                errors.append("A station cannot appear twice on the same schedule. ");
                break;
            }
        }

        if (schedule.getRoute() != null
                && schedule.getRoute().getStartStation() != null
                && schedule.getRoute().getEndStation() != null
                && stops.get(0).getStation() != null
                && stops.get(stops.size() - 1).getStation() != null) {

            int firstId = stops.get(0).getStation().getId();
            int lastId  = stops.get(stops.size() - 1).getStation().getId();
            int routeStartId = schedule.getRoute().getStartStation().getId();
            int routeEndId   = schedule.getRoute().getEndStation().getId();

            if (firstId != routeStartId) {
                errors.append("First stop must be the route's start station. ");
            }
            if (lastId != routeEndId) {
                errors.append("Last stop must be the route's end station. ");
            }
        }

        for (int i = 0; i < stops.size(); i++) {
            ScheduleStop st = stops.get(i);
            boolean isFirst = (i == 0);
            boolean isLast  = (i == stops.size() - 1);

            if (isFirst) {
                if (st.getDepartureTime() == null) {
                    errors.append("First stop must have a departure time. ");
                }
                if (st.getArrivalTime() != null) {
                    errors.append("First stop should not have an arrival time. ");
                }
            } else if (isLast) {
                if (st.getArrivalTime() == null) {
                    errors.append("Last stop must have an arrival time. ");
                }
                if (st.getDepartureTime() != null) {
                    errors.append("Last stop should not have a departure time. ");
                }
            } else {
                if (st.getArrivalTime() == null || st.getDepartureTime() == null) {
                    errors.append("Intermediate stops must have both arrival and departure times. ");
                }
            }

            if (st.getArrivalTime() != null && st.getDepartureTime() != null
                    && st.getArrivalTime().isAfter(st.getDepartureTime())) {
                errors.append("Arrival must not be after departure at the same stop. ");
            }
        }

        for (int i = 0; i < stops.size() - 1; i++) {
            ScheduleStop a = stops.get(i);
            ScheduleStop b = stops.get(i + 1);
            if (a.getDepartureTime() != null && b.getArrivalTime() != null
                    && a.getDepartureTime().isAfter(b.getArrivalTime())) {
                errors.append("Times must be non-decreasing between consecutive stops. ");
                break;
            }
        }

        if (schedule.getDelayMinutes() < 0) {
            errors.append("Delay minutes cannot be negative. ");
        }

        if (schedule.getStatus() == null || schedule.getStatus().trim().isEmpty()) {
            errors.append("Status is required (e.g. ON-TIME). ");
        } else if (schedule.getStatus().length() > 20) {
            errors.append("Status can be at most 20 characters. ");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString().trim());
        }
    }
}
