package validators;

import domain.Station;
import exceptions.ValidationException;

public class StationValidator implements IValidator<Station> {
    @Override
    public void validate(Station station) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        String name = station.getStationName();
        if (name == null || name.trim().isEmpty()) {
            errors.append("Station name is mandatory. ");
        } else if (name.length() > 50) {
            errors.append("Station name can be at most 50 characters. ");
        }

        String city = station.getStationCity();
        if (city == null || city.trim().isEmpty()) {
            errors.append("Station city is mandatory. ");
        } else if (city.length() > 50) {
            errors.append("Station city can be at most 50 characters. ");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString().trim());
        }
    }
}
