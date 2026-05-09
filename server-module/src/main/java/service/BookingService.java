package service;

import domain.Booking;
import domain.Schedule;
import domain.ScheduleStop;
import domain.Station;
import domain.User;
import dtos.AvailableScheduleDTO;
import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import dtos.DTOUtils;
import dtos.JourneyDTO;
import dtos.JourneyLegDTO;
import dtos.JourneySearchDTO;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import repository.BookingRepository;
import repository.ScheduleRepository;
import repository.StationRepository;
import repository.UserRepository;
import validators.BookingValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final BookingValidator validator;
    private final EmailService emailService;

    public BookingService(BookingRepository bookingRepository,
                          ScheduleRepository scheduleRepository,
                          StationRepository stationRepository,
                          UserRepository userRepository,
                          BookingValidator validator,
                          EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
        this.validator = validator;
        this.emailService = emailService;
    }

    public Booking book(BookingRequestDTO request) throws ValidationException, NotFoundException {
        validator.validate(request);

        User user = userRepository.findUserByUsername(request.getUsername());
        if (user == null) throw new NotFoundException("User '" + request.getUsername() + "' not found.");

        Schedule schedule = scheduleRepository.findById(request.getScheduleId());
        if (schedule == null) throw new NotFoundException("Schedule not found.");

        if ("CANCELLED".equalsIgnoreCase(schedule.getStatus())) {
            throw new ValidationException("This schedule is cancelled, you cannot book seats on it.");
        }

        int startIdx = stopIndexOf(schedule, request.getStartStationId());
        int endIdx = stopIndexOf(schedule, request.getEndStationId());
        if (startIdx < 0 || endIdx < 0) {
            throw new ValidationException("Pick stations that exist on this schedule's path.");
        }
        if (startIdx >= endIdx) {
            throw new ValidationException("Start station must come before end station on the schedule.");
        }

        int capacity = schedule.getTrain() == null ? 0 : schedule.getTrain().getCapacity();
        if (capacity <= 0) {
            throw new ValidationException("This schedule's train has no capacity configured.");
        }

        List<Booking> existing = bookingRepository.findBySchedule(schedule.getId());
        for (int leg = startIdx; leg < endIdx; leg++) {
            int legSeats = request.getSeatsReserved();
            for (Booking b : existing) {
                int bStart = stopIndexOf(schedule, b.getStartStation().getId());
                int bEnd   = stopIndexOf(schedule, b.getEndStation().getId());
                if (bStart < 0 || bEnd < 0) continue;
                if (bStart <= leg && leg < bEnd) {
                    legSeats += b.getSeatsReserved();
                }
            }
            if (legSeats > capacity) {
                throw new ValidationException(
                        "Not enough free seats on this segment. Capacity " + capacity
                                + ", requested + already booked: " + legSeats + ".");
            }
        }

        Station start = stationRepository.findStationById(request.getStartStationId());
        Station end = stationRepository.findStationById(request.getEndStationId());
        Booking booking = new Booking(
                user, schedule, start, end,
                request.getSeatsReserved(),
                LocalDateTime.now()
        );
        Booking saved = bookingRepository.save(booking);

        Booking refreshed = bookingRepository.findById(saved.getId());
        Booking forEmail = refreshed != null ? refreshed : saved;
        try {
            emailService.sendBookingConfirmation(request.getPassengerEmail(), forEmail);
        } catch (RuntimeException ignored) {
            System.err.println("Email delivery failed for booking #" + forEmail.getId()
                    + ", booking remains valid.");
        }

        return forEmail;
    }

    public List<BookingDTO> getAllBookings() {
        List<Booking> all = bookingRepository.findAll();
        List<BookingDTO> dtos = new ArrayList<>(all.size());
        for (Booking b : all) dtos.add(DTOUtils.getDTO(b));
        return dtos;
    }

    public List<BookingDTO> getBookingsForUser(String username) {
        List<Booking> all = bookingRepository.findByUsername(username);
        List<BookingDTO> dtos = new ArrayList<>(all.size());
        for (Booking b : all) dtos.add(DTOUtils.getDTO(b));
        return dtos;
    }

    public List<AvailableScheduleDTO> search(JourneySearchDTO criteria) {
        if (criteria == null
                || criteria.getDate() == null
                || criteria.getStartStationId() <= 0
                || criteria.getEndStationId() <= 0
                || criteria.getStartStationId() == criteria.getEndStationId()) {
            return List.of();
        }

        LocalDate date = criteria.getDate();
        int startId = criteria.getStartStationId();
        int endId = criteria.getEndStationId();

        List<Schedule> all = scheduleRepository.getAllSchedules();
        List<AvailableScheduleDTO> results = new ArrayList<>();

        for (Schedule s : all) {
            if (s.getDepartureTime() == null) continue;
            if (!s.getDepartureTime().toLocalDate().equals(date)) continue;
            if ("CANCELLED".equalsIgnoreCase(s.getStatus())) continue;

            ScheduleStop startStop = null;
            ScheduleStop endStop = null;
            int startIdx = -1;
            int endIdx = -1;
            int idx = 0;
            for (ScheduleStop stop : s.getStops()) {
                if (stop.getStation().getId() == startId) {
                    startIdx = idx;
                    startStop = stop;
                }
                if (stop.getStation().getId() == endId) {
                    endIdx = idx;
                    endStop = stop;
                }
                idx++;
            }
            if (startIdx < 0 || endIdx < 0 || startIdx >= endIdx) continue;

            int capacity = s.getTrain() == null ? 0 : s.getTrain().getCapacity();
            int seatsAvailable = capacity;
            if (capacity > 0) {
                List<Booking> bookings = bookingRepository.findBySchedule(s.getId());
                int maxSeatsTaken = 0;
                for (int leg = startIdx; leg < endIdx; leg++) {
                    int legSeats = 0;
                    for (Booking b : bookings) {
                        int bStart = stopIndexOf(s, b.getStartStation().getId());
                        int bEnd = stopIndexOf(s, b.getEndStation().getId());
                        if (bStart < 0 || bEnd < 0) continue;
                        if (bStart <= leg && leg < bEnd) {
                            legSeats += b.getSeatsReserved();
                        }
                    }
                    if (legSeats > maxSeatsTaken) maxSeatsTaken = legSeats;
                }
                seatsAvailable = capacity - maxSeatsTaken;
            }

            results.add(new AvailableScheduleDTO(
                    s.getId(),
                    s.getTrain() == null ? 0 : s.getTrain().getId(),
                    s.getTrain() == null ? null : s.getTrain().getTrainNumber(),
                    capacity,
                    startStop.getStation().getId(),
                    startStop.getStation().getStationName(),
                    startStop.getStation().getStationCity(),
                    startStop.getDepartureTime(),
                    endStop.getStation().getId(),
                    endStop.getStation().getStationName(),
                    endStop.getStation().getStationCity(),
                    endStop.getArrivalTime(),
                    s.getDelayMinutes(),
                    s.getStatus(),
                    seatsAvailable
            ));
        }

        results.sort((a, b) -> {
            if (a.getDepartureTime() == null) return 1;
            if (b.getDepartureTime() == null) return -1;
            return a.getDepartureTime().compareTo(b.getDepartureTime());
        });
        return results;
    }

    private int stopIndexOf(Schedule s, int stationId) {
        int i = 0;
        for (ScheduleStop stop : s.getStops()) {
            if (stop.getStation() != null && stop.getStation().getId() == stationId) return i;
            i++;
        }
        return -1;
    }

    private static final int MIN_TRANSFER_MINUTES = 5;
    private static final int MAX_LAYOVER_HOURS = 6;
    private static final int MAX_RESULTS = 25;

    public List<JourneyDTO> searchJourneys(JourneySearchDTO criteria) {
        if (criteria == null
                || criteria.getDate() == null
                || criteria.getStartStationId() <= 0
                || criteria.getEndStationId() <= 0
                || criteria.getStartStationId() == criteria.getEndStationId()) {
            return List.of();
        }

        LocalDate date = criteria.getDate();
        int startId = criteria.getStartStationId();
        int endId = criteria.getEndStationId();

        List<Schedule> all = scheduleRepository.getAllSchedules();
        Map<Integer, Map<Integer, Integer>> indexById = new HashMap<>();
        Map<Integer, List<Booking>> bookingsByScheduleId = new HashMap<>();
        for (Schedule s : all) {
            Map<Integer, Integer> idx = new HashMap<>();
            int i = 0;
            for (ScheduleStop stop : s.getStops()) {
                if (stop.getStation() != null) {
                    idx.put(stop.getStation().getId(), i);
                }
                i++;
            }
            indexById.put(s.getId(), idx);
        }

        List<JourneyDTO> results = new ArrayList<>();

        for (Schedule s : all) {
            if (!startsOn(s, date) || isCancelled(s)) continue;
            Integer startIdx = indexById.get(s.getId()).get(startId);
            Integer endIdx = indexById.get(s.getId()).get(endId);
            if (startIdx == null || endIdx == null || startIdx >= endIdx) continue;

            JourneyLegDTO leg = buildLeg(s, startIdx, endIdx, bookingsByScheduleId);
            results.add(new JourneyDTO(List.of(leg)));
        }

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

                LocalDateTime arrAtX = xStop1.getArrivalTime();
                if (arrAtX == null) continue;

                for (Schedule s2 : all) {
                    if (s2.getId() == s1.getId()) continue;
                    if (isCancelled(s2)) continue;

                    Map<Integer, Integer> s2Idx = indexById.get(s2.getId());
                    Integer s2XIdx = s2Idx.get(xId);
                    Integer s2EndIdx = s2Idx.get(endId);
                    if (s2XIdx == null || s2EndIdx == null || s2XIdx >= s2EndIdx) continue;

                    ScheduleStop xStop2 = s2.getStops().get(s2XIdx);
                    LocalDateTime depFromX = xStop2.getDepartureTime();
                    if (depFromX == null) continue;
                    if (!depFromX.isAfter(arrAtX.plusMinutes(MIN_TRANSFER_MINUTES))) continue;
                    if (depFromX.isAfter(arrAtX.plusHours(MAX_LAYOVER_HOURS))) continue;

                    JourneyLegDTO leg1 = buildLeg(s1, s1StartIdx, xIdx, bookingsByScheduleId);
                    JourneyLegDTO leg2 = buildLeg(s2, s2XIdx, s2EndIdx, bookingsByScheduleId);
                    results.add(new JourneyDTO(List.of(leg1, leg2)));
                }
            }
        }

        results.sort((a, b) -> {
            LocalDateTime aa = a.getOverallArrival();
            LocalDateTime bb = b.getOverallArrival();
            if (aa == null) return 1;
            if (bb == null) return -1;
            int cmp = aa.compareTo(bb);
            if (cmp != 0) return cmp;
            return Integer.compare(a.getNumberOfChangeovers(), b.getNumberOfChangeovers());
        });

        if (results.size() > MAX_RESULTS) {
            return new ArrayList<>(results.subList(0, MAX_RESULTS));
        }
        return results;
    }

    private boolean startsOn(Schedule s, LocalDate date) {
        return s.getDepartureTime() != null
                && s.getDepartureTime().toLocalDate().equals(date);
    }

    private boolean isCancelled(Schedule s) {
        return "CANCELLED".equalsIgnoreCase(s.getStatus());
    }

    private JourneyLegDTO buildLeg(Schedule s, int fromIdx, int toIdx,
                                   Map<Integer, List<Booking>> bookingsCache) {
        ScheduleStop fromStop = s.getStops().get(fromIdx);
        ScheduleStop toStop = s.getStops().get(toIdx);

        int capacity = s.getTrain() == null ? 0 : s.getTrain().getCapacity();
        int seatsAvailable = capacity;
        if (capacity > 0) {
            List<Booking> bookings = bookingsCache.computeIfAbsent(
                    s.getId(), bookingRepository::findBySchedule);
            int maxSeatsTaken = 0;
            for (int leg = fromIdx; leg < toIdx; leg++) {
                int legSeats = 0;
                for (Booking b : bookings) {
                    int bStart = stopIndexOf(s, b.getStartStation().getId());
                    int bEnd = stopIndexOf(s, b.getEndStation().getId());
                    if (bStart < 0 || bEnd < 0) continue;
                    if (bStart <= leg && leg < bEnd) {
                        legSeats += b.getSeatsReserved();
                    }
                }
                if (legSeats > maxSeatsTaken) maxSeatsTaken = legSeats;
            }
            seatsAvailable = capacity - maxSeatsTaken;
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
}
