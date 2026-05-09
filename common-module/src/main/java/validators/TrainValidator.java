package validators;

import domain.Train;
import exceptions.ValidationException;

public class TrainValidator implements IValidator<Train> {
    @Override
    public void validate(Train train) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        String number = train.getTrainNumber();
        if (number == null || number.trim().isEmpty()) {
            errors.append("Train number is mandatory. ");
        } else if (number.length() > 20) {
            errors.append("Train number can be at most 20 characters. ");
        }

        if (train.getCapacity() <= 0) {
            errors.append("Train capacity must be greater than 0. ");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString().trim());
        }
    }
}
