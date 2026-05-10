package service;

import dtos.AvailableScheduleDTO;
import dtos.BookingDTO;
import dtos.BookingRequestDTO;
import dtos.JourneyDTO;
import dtos.JourneySearchDTO;
import exceptions.AppException;

import java.util.List;

/**
 * Customer-facing search and booking, plus admin-side booking inspection.
 */
public interface IBookingService {

    List<AvailableScheduleDTO> searchAvailableSchedules(JourneySearchDTO criteria);
    List<JourneyDTO> searchJourneys(JourneySearchDTO criteria);
    BookingDTO bookSeats(BookingRequestDTO request) throws AppException;
    List<BookingDTO> getAllBookings();
    List<BookingDTO> getBookingsForUser(String username);
}
