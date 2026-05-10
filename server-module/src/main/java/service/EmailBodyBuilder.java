package service;

import domain.Booking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class EmailBodyBuilder {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private EmailBodyBuilder() {}

    static String forBookingConfirmation(Booking b) {
        return String.format(
                "Hello %s,%n%n" +
                        "Your booking has been confirmed.%n%n" +
                        "Booking ID:   %d%n" +
                        "Train:        %s%n" +
                        "From:         %s · %s%n" +
                        "Departure:    %s%n" +
                        "To:           %s · %s%n" +
                        "Arrival:      %s%n" +
                        "Seats:        %d%n" +
                        "Booked on:    %s%n%n" +
                        "Have a pleasant journey.%n",
                username(b),
                b.getId(),
                trainNumber(b),
                b.getStartStation().getStationCity(), b.getStartStation().getStationName(),
                fmt(b.getSchedule() == null ? null : b.getSchedule().getDepartureTime()),
                b.getEndStation().getStationCity(), b.getEndStation().getStationName(),
                fmt(b.getSchedule() == null ? null : b.getSchedule().getArrivalTime()),
                b.getSeatsReserved(),
                fmt(b.getBookingDate())
        );
    }

    static String forDelayNotification(String trainNumber, int delayMinutes, Booking b) {
        return String.format(
                "Hello %s,%n%n" +
                        "We are sorry to inform you that train %s has been delayed by %d minute(s).%n%n" +
                        "Affected booking%n" +
                        "----------------%n" +
                        "Booking ID:           %d%n" +
                        "Train:                %s%n" +
                        "From:                 %s · %s%n" +
                        "To:                   %s · %s%n" +
                        "Originally scheduled: %s%n" +
                        "Estimated delay:      %d min%n%n" +
                        "We apologise for the inconvenience.%n",
                username(b),
                trainNumber, delayMinutes,
                b.getId(), trainNumber,
                b.getStartStation().getStationCity(), b.getStartStation().getStationName(),
                b.getEndStation().getStationCity(), b.getEndStation().getStationName(),
                fmt(b.getSchedule() == null ? null : b.getSchedule().getDepartureTime()),
                delayMinutes
        );
    }

    static String forCancellationNotification(String trainNumber, Booking b) {
        return String.format(
                "Hello %s,%n%n" +
                        "We are sorry to inform you that train %s has been CANCELLED.%n%n" +
                        "Affected booking%n" +
                        "----------------%n" +
                        "Booking ID:    %d%n" +
                        "Train:         %s%n" +
                        "From:          %s · %s%n" +
                        "To:            %s · %s%n" +
                        "Was scheduled: %s%n%n" +
                        "Please contact our support to reschedule or refund.%n" +
                        "We apologise for the inconvenience.%n",
                username(b),
                trainNumber,
                b.getId(), trainNumber,
                b.getStartStation().getStationCity(), b.getStartStation().getStationName(),
                b.getEndStation().getStationCity(), b.getEndStation().getStationName(),
                fmt(b.getSchedule() == null ? null : b.getSchedule().getDepartureTime())
        );
    }

    private static String username(Booking b) {
        return b.getUser() == null ? "passenger" : b.getUser().getUsername();
    }

    private static String trainNumber(Booking b) {
        if (b.getSchedule() == null || b.getSchedule().getTrain() == null) return "—";
        return b.getSchedule().getTrain().getTrainNumber();
    }

    private static String fmt(LocalDateTime t) {
        return t == null ? "—" : t.format(FMT);
    }
}
