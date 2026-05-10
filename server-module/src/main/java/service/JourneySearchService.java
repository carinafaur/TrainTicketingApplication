package service;

import domain.Booking;
import domain.Schedule;
import domain.ScheduleStop;
import dtos.AvailableScheduleDTO;
import dtos.JourneyDTO;
import dtos.JourneyLegDTO;
import dtos.JourneySearchDTO;
import repository.BookingRepository;
import repository.ScheduleRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JourneySearchService {

    private static final int MIN_TRANSFER_MINUTES = 5;
    private static final int MAX_LAYOVER_HOURS = 6;
    private static final int MAX_RESULTS = 25;

    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;

    public JourneySearchService(ScheduleRepository scheduleRepository,
                                BookingRepository bookingRepository) {
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<AvailableScheduleDTO> searchDirect(JourneySearchDTO criteria) {
        if (!isValid(criteria)) return List.of();

        LocalDate date = criteria.getDate();
        int startId = criteria.getStartStationId();
        int endId = criteria.getEndStationId();

        List<AvailableScheduleDTO> results = new ArrayList<>();
        for (Schedule s : scheduleRepository.getAllSchedules()) {
            if (!startsOn(s, date) || isCancelled(s)) continue;

            int startIdx = SeatAvailability.stopIndexOf(s, startId);
            int endIdx   = SeatAvailability.stopIndexOf(s, endId);
            if (startIdx < 0 || endIdx < 0 || startIdx >= endIdx) continue;

            JourneyLegDTO leg = buildLeg(s, startIdx, endIdx);
            results.add(toAvailableSchedule(leg));
        }
        results.sort((a, b) -> nullSafeCompare(a.getDepartureTime(), b.getDepartureTime()));
        return results;
    }

    public List<JourneyDTO> searchJourneys(JourneySearchDTO criteria) {
        if (!isValid(criteria)) return List.of();

        LocalDate date = criteria.getDate();
        int startId = criteria.getStartStationId();
        int endId = criteria.getEndStationId();

        List<Schedule> all = scheduleRepository.getAllSchedules();
        Map<Integer, Map<Integer, Integer>> indexById = buildStationIndices(all);

        List<JourneyDTO> results = new ArrayList<>();
        addDirectJourneys(all, indexById, date, startId, endId, results);
        addOneChangeoverJourneys(all, indexById, date, startId, endId, results);

        results.sort((a, b) -> {
            int cmp = nullSafeCompare(a.getOverallArrival(), b.getOverallArrival());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getNumberOfChangeovers(), b.getNumberOfChangeovers());
        });

        if (results.size() > MAX_RESULTS) {
            return new ArrayList<>(results.subList(0, MAX_RESULTS));
        }
        return results;
    }

    private void addDirectJourneys(List<Schedule> all,
                                   Map<Integer, Map<Integer, Integer>> indexById,
                                   LocalDate date, int startId, int endId,
                                   List<JourneyDTO> results) {
        for (Schedule s : all) {
            if (!startsOn(s, date) || isCancelled(s)) continue;

            Integer startIdx = indexById.get(s.getId()).get(startId);
            Integer endIdx   = indexById.get(s.getId()).get(endId);
            if (startIdx == null || endIdx == null || startIdx >= endIdx) continue;

            results.add(new JourneyDTO(List.of(buildLeg(s, startIdx, endIdx))));
        }
    }

    private void addOneChangeoverJourneys(List<Schedule> all,
                                          Map<Integer, Map<Integer, Integer>> indexById,
                                          LocalDate date, int startId, int endId,
                                          List<JourneyDTO> results) {
        for (Schedule s1 : all) {
            if (!startsOn(s1, date) || isCancelled(s1)) continue;
            Integer s1StartIdx = indexById.get(s1.getId()).get(startId);
            if (s1StartIdx == null) continue;

            List<ScheduleStop> stops1 = s1.getStops();
            for (int xIdx = s1StartIdx + 1; xIdx < stops1.size(); xIdx++) {
                ScheduleStop xStop1 = stops1.get(xIdx);
                if (xStop1.getStation() == null) continue;
                int xId = xStop1.getStation().getId();
                if (xId == endId || xId == startId) continue;
                if (xStop1.getArrivalTime() == null) continue;

                for (Schedule s2 : all) {
                    if (s2.getId() == s1.getId() || isCancelled(s2)) continue;

                    Map<Integer, Integer> s2Index = indexById.get(s2.getId());
                    Integer s2XIdx   = s2Index.get(xId);
                    Integer s2EndIdx = s2Index.get(endId);
                    if (s2XIdx == null || s2EndIdx == null || s2XIdx >= s2EndIdx) continue;

                    ScheduleStop xStop2 = s2.getStops().get(s2XIdx);
                    if (xStop2.getDepartureTime() == null) continue;
                    if (!xStop2.getDepartureTime().isAfter(
                            xStop1.getArrivalTime().plusMinutes(MIN_TRANSFER_MINUTES))) continue;
                    if (xStop2.getDepartureTime().isAfter(
                            xStop1.getArrivalTime().plusHours(MAX_LAYOVER_HOURS))) continue;

                    results.add(new JourneyDTO(List.of(
                            buildLeg(s1, s1StartIdx, xIdx),
                            buildLeg(s2, s2XIdx, s2EndIdx)
                    )));
                }
            }
        }
    }

    private JourneyLegDTO buildLeg(Schedule s, int fromIdx, int toIdx) {
        ScheduleStop fromStop = s.getStops().get(fromIdx);
        ScheduleStop toStop = s.getStops().get(toIdx);

        int capacity = s.getTrain() == null ? 0 : s.getTrain().getCapacity();
        int seatsAvailable = capacity;
        if (capacity > 0) {
            List<Booking> bookings = bookingRepository.findBySchedule(s.getId());
            int peak = SeatAvailability.maxSeatsTakenInRange(s, bookings, fromIdx, toIdx);
            seatsAvailable = capacity - peak;
        }

        return new JourneyLegDTO(
                s.getId(),
                s.getTrain() == null ? 0 : s.getTrain().getId(),
                s.getTrain() == null ? null : s.getTrain().getTrainNumber(),
                fromStop.getStation().getId(),
                fromStop.getStation().getStationName(),
                fromStop.getStation().getStationCity(),
                fromStop.getDepartureTime(),
                toStop.getStation().getId(),
                toStop.getStation().getStationName(),
                toStop.getStation().getStationCity(),
                toStop.getArrivalTime(),
                seatsAvailable,
                s.getDelayMinutes(),
                s.getStatus()
        );
    }

    private static AvailableScheduleDTO toAvailableSchedule(JourneyLegDTO leg) {
        return new AvailableScheduleDTO(
                leg.getScheduleId(),
                leg.getTrainId(),
                leg.getTrainNumber(),
                /* trainCapacity is not surfaced in the leg DTO; admins see this elsewhere */ 0,
                leg.getFromStationId(),
                leg.getFromStationName(),
                leg.getFromStationCity(),
                leg.getDepartureTime(),
                leg.getToStationId(),
                leg.getToStationName(),
                leg.getToStationCity(),
                leg.getArrivalTime(),
                leg.getDelayMinutes(),
                leg.getStatus(),
                leg.getSeatsAvailable()
        );
    }

    private static Map<Integer, Map<Integer, Integer>> buildStationIndices(List<Schedule> all) {
        Map<Integer, Map<Integer, Integer>> result = new HashMap<>();
        for (Schedule s : all) {
            Map<Integer, Integer> idx = new HashMap<>();
            int i = 0;
            for (ScheduleStop stop : s.getStops()) {
                if (stop.getStation() != null) {
                    idx.put(stop.getStation().getId(), i);
                }
                i++;
            }
            result.put(s.getId(), idx);
        }
        return result;
    }

    private static boolean isValid(JourneySearchDTO criteria) {
        return criteria != null
                && criteria.getDate() != null
                && criteria.getStartStationId() > 0
                && criteria.getEndStationId() > 0
                && criteria.getStartStationId() != criteria.getEndStationId();
    }

    private static boolean startsOn(Schedule s, LocalDate date) {
        return s.getDepartureTime() != null
                && s.getDepartureTime().toLocalDate().equals(date);
    }

    private static boolean isCancelled(Schedule s) {
        return "CANCELLED".equalsIgnoreCase(s.getStatus());
    }

    private static <T extends Comparable<T>> int nullSafeCompare(T a, T b) {
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }
}
