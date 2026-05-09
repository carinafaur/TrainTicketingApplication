package validators;

import exceptions.ValidationException;

/**
 * Domain-level validator. One implementation per entity type. Validators are
 * stateless and safe to share between threads.
 *
 * @param <T> the entity type being validated
 */
public interface IValidator<T> {

    void validate(T entity) throws ValidationException;
}
