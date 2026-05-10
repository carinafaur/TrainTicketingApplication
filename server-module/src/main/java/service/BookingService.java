package service;

import domain.Booking;
import domain.Schedule;
import domain.Station;
import domain.User;
import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import dtos.DTOUtils;
import exceptions.NotFoundException;
import exceptions.ValidationException;
import repository.BookingRepository;
import repository.ScheduleRepository;
import repository.StationRepository;
import repository.UserRepository;
import validators.BookingValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        int startIdx = SeatAvailability.stopIndexOf(schedule, request.getStartStationId());
        int endIdx   = SeatAvailability.stopIndexOf(schedule, request.getEndStationId());
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
        int peak = SeatAvailability.maxSeatsTakenInRange(schedule, existing, startIdx, endIdx);
        if (peak + request.getSeatsReserved() > capacity) {
            throw new ValidationException(
                    "Not enough free seats on this segment. Capacity " + capacity
                            + ", peak demand after this booking would be "
                            + (peak + request.getSeatsReserved()) + ".");
        }

        Station start = stationRepository.findStationById(request.getStartStationId());
        Station end = stationRepository.findStationById(request.getEndStationId());
        Booking booking = new Booking(user, schedule, start, end,
                request.getSeatsReserved(), LocalDateTime.now());
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
        return toDTOs(bookingRepository.findAll());
    }

    public List<BookingDTO> getBookingsForUser(String username) {
        return toDTOs(bookingRepository.findByUsername(username));
    }

    public void notifyDelayedSchedule(Schedule schedule) {
        String trainNumber = trainNumberOf(schedule);
        int delayMinutes = schedule.getDelayMinutes();

        for (Booking b : bookingRepository.findBySchedule(schedule.getId())) {
            String email = recipientFor(b);
            if (email == null) continue;
            try {
                emailService.sendDelayNotification(email, trainNumber, delayMinutes, b);
            } catch (RuntimeException e) {
                System.err.println("BookingService: delay-notify failed for " + email
                        + " (booking #" + b.getId() + "): " + e.getMessage());
            }
        }
    }

    public void notifyCancelledSchedule(Schedule schedule) {
        String trainNumber = trainNumberOf(schedule);

        for (Booking b : bookingRepository.findBySchedule(schedule.getId())) {
            String email = recipientFor(b);
            if (email == null) continue;
            try {
                emailService.sendCancellationNotification(email, trainNumber, b);
            } catch (RuntimeException e) {
                System.err.println("BookingService: cancel-notify failed for " + email
                        + " (booking #" + b.getId() + "): " + e.getMessage());
            }
        }
    }

    private static List<BookingDTO> toDTOs(List<Booking> bookings) {
        List<BookingDTO> dtos = new ArrayList<>(bookings.size());
        for (Booking b : bookings) dtos.add(DTOUtils.getDTO(b));
        return dtos;
    }

    private static String trainNumberOf(Schedule s) {
        if (s.getTrain() == null) return "(unknown)";
        return s.getTrain().getTrainNumber();
    }

    private static String recipientFor(Booking b) {
        if (b.getUser() == null) return null;
        String email = b.getUser().getEmail();
        return (email == null || email.isBlank()) ? null : email;
    }
}
