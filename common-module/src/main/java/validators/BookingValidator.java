package validators;

import dtos.BookingRequestDTO;
import exceptions.ValidationException;

public class BookingValidator implements IValidator<BookingRequestDTO> {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public void validate(BookingRequestDTO dto) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            errors.append("Username is required. ");
        }
        if (dto.getScheduleId() <= 0) {
            errors.append("A schedule must be selected. ");
        }
        if (dto.getStartStationId() <= 0) {
            errors.append("Start station is required. ");
        }
        if (dto.getEndStationId() <= 0) {
            errors.append("End station is required. ");
        }
        if (dto.getStartStationId() == dto.getEndStationId()
                && dto.getStartStationId() != 0) {
            errors.append("Start and end stations must be different. ");
        }
        if (dto.getSeatsReserved() <= 0) {
            errors.append("You must reserve at least 1 seat. ");
        }
        if (dto.getSeatsReserved() > 100) {
            errors.append("Cannot reserve more than 100 seats per booking. ");
        }

        String email = dto.getPassengerEmail();
        if (email == null || email.trim().isEmpty()) {
            errors.append("Passenger email is required for confirmation. ");
        } else if (!email.matches(EMAIL_REGEX)) {
            errors.append("Passenger email is not a valid email address. ");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString().trim());
        }
    }
}
