package validators;

import exceptions.ValidationException;

/**
 * Domain-level validator. One implementation per entity type
 * ({@code RouteValidator}, {@code TrainValidator}, ...). Validators are
 * stateless and safe to share between threads.
 *
 * @param <T> the entity type being validated
 */
public interface IValidator<T> {

    /**
     * Verifies that {@code entity} satisfies all domain invariants.
     *
     * @param entity the entity to validate; never {@code null}
     * @throws ValidationException with a human-readable message describing
     *                             every rule that was violated
     */
    void validate(T entity) throws ValidationException;
}
