package validators;

import exceptions.ValidationException;

public interface IValidator<T> {
    public void validate(T entity) throws ValidationException;
}
