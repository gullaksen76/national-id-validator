package no.example.nationalidvalidator.model;

import lombok.Builder;
import lombok.Value;

/** Immutable result produced by a validator. */
@Value
@Builder
public class ValidationResult {

    private boolean valid;
    private IdType idType;
    private String message;

    /**
     * Creates a successful validation result.
     *
     * @param idType the identified type of the number
     * @return a valid result
     */
    public static ValidationResult valid(final IdType idType) {
        return ValidationResult.builder()
                .valid(true)
                .idType(idType)
                .message("Valid")
                .build();
    }

    /**
     * Creates a failed validation result.
     *
     * @param message a human-readable description of why validation failed
     * @return an invalid result
     */
    public static ValidationResult invalid(final String message) {
        return ValidationResult.builder()
                .valid(false)
                .idType(null)
                .message(message)
                .build();
    }
}
