package de.hs_esslingen.besy.configurations;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component @AllArgsConstructor
public class ValidationHelper {

    private final Validator validator;


    /**
     * Validates the given DTO using Bean Validation annotations.
     *
     * @param dto the object to validate
     * @param <T> the type of the object
     * @throws ConstraintViolationException if any validation constraints are violated
     */
    public <T> void validateOrThrow(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
