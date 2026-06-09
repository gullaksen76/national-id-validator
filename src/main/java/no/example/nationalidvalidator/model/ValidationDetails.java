package no.example.nationalidvalidator.model;

import lombok.Builder;
import lombok.Value;

/**
 * Detailed breakdown of a national identity number validation.
 *
 * <p>Provides granular information about why a number passed or failed validation.
 */
@Value
@Builder
public class ValidationDetails {

    /** Whether the input is exactly 11 characters long. */
    private boolean elevenDigits;

    /** Whether the input contains only numeric characters. */
    private boolean numericOnly;

    /** The identified type of the number (or null if format/structure is invalid). */
    private IdType idType;

    /** Whether the date/structure in the number is valid according to Norwegian rules. */
    private boolean validStructure;

    /**
     * Which control digit calculation regime(s) validate this number.
     * {@code NONE} means no regime validates it.
     */
    private ControlDigitRegime controlDigitRegime;

    /**
     * Creates a validation details with all checks passed.
     *
     * @param idType the identified type
     * @param controlDigitRegime the matching control digit regime
     * @return fully valid details
     */
    public static ValidationDetails valid(
            final IdType idType,
            final ControlDigitRegime controlDigitRegime) {
        return ValidationDetails.builder()
                .elevenDigits(true)
                .numericOnly(true)
                .idType(idType)
                .validStructure(true)
                .controlDigitRegime(controlDigitRegime)
                .build();
    }

    /**
     * Returns whether this number is fully valid (all checks pass).
     */
    public boolean isValid() {
        return elevenDigits
                && numericOnly
                && idType != null
                && validStructure
                && controlDigitRegime != ControlDigitRegime.NONE;
    }

    /**
     * Returns a user-friendly error message describing why validation failed.
     * Returns an empty string if the number is valid.
     */
    public String getErrorMessage() {
        if (isValid()) {
            return "";
        }

        if (!elevenDigits) {
            return "Invalid format: must be exactly 11 digits";
        }

        if (!numericOnly) {
            return "Invalid format: must contain only numeric characters";
        }

        if (!validStructure) {
            return "Invalid date in number";
        }

        if (controlDigitRegime == ControlDigitRegime.NONE) {
            return "Invalid check digits";
        }

        return "Unknown validation error";
    }
}
