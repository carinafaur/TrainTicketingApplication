package service;

import domain.Booking;
import domain.Schedule;
import domain.ScheduleStop;

import java.util.List;

final class SeatAvailability {

    private SeatAvailability() {}

    static int stopIndexOf(Schedule s, int stationId) {
        int i = 0;
        for (ScheduleStop stop : s.getStops()) {
            if (stop.getStation() != null && stop.getStation().getId() == stationId) return i;
            i++;
        }
        return -1;
    }

    static int maxSeatsTakenInRange(Schedule schedule,
                                    List<Booking> existingBookings,
                                    int fromIdx, int toIdx) {
        int max = 0;
        for (int leg = fromIdx; leg < toIdx; leg++) {
            int legSeats = 0;
            for (Booking b : existingBookings) {
                int bStart = stopIndexOf(schedule, b.getStartStation().getId());
                int bEnd   = stopIndexOf(schedule, b.getEndStation().getId());
                if (bStart < 0 || bEnd < 0) continue;
                if (bStart <= leg && leg < bEnd) {
                    legSeats += b.getSeatsReserved();
                }
            }
            if (legSeats > max) max = legSeats;
        }
        return max;
    }
}
