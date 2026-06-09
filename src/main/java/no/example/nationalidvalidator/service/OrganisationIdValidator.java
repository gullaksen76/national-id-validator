package no.example.nationalidvalidator.service;

import lombok.extern.slf4j.Slf4j;
import no.example.nationalidvalidator.model.IdType;
import no.example.nationalidvalidator.model.ValidationResult;

/**
 * Validates Norwegian organization numbers (organisasjonsnummer).
 *
 * <p>An organization number consists of 9 digits where the first digit must be
 * 8 or 9, and the last digit is a control digit computed using a weighted mod-11 sum.
 * The algorithm is specified by Brønnøysundregistrene.
 *
 * <p>This class is stateless and thread-safe. A single instance can be shared freely.
 */
@Slf4j
public class OrganisationIdValidator {

    private static final int EXPECTED_LENGTH = 9;
    private static final int MODULUS = 11;
    private static final int MIN_FIRST_DIGIT = 8;

    /** Weights used to compute the control digit. */
    private static final int[] WEIGHTS = {3, 2, 7, 6, 5, 4, 3, 2};

    /**
     * Validates a Norwegian organization number.
     *
     * @param number the 9-digit string to validate
     * @return a {@link ValidationResult} describing whether the number is valid
     */
    public ValidationResult validate(final String number) {
        log.debug("Validating organization number: {}", number);

        if (!hasValidFormat(number)) {
            return ValidationResult.invalid("Invalid format: must be exactly 9 digits");
        }

        final int[] digits = toDigits(number);

        if (!hasValidFirstDigit(digits)) {
            return ValidationResult.invalid(
                    "Invalid first digit: organization number must start with 8 or 9");
        }

        if (!hasValidCheckDigit(digits)) {
            return ValidationResult.invalid("Invalid check digit");
        }

        log.debug("Organization number {} is valid", number);
        return ValidationResult.valid(IdType.ORGANISASJONSNUMMER);
    }

    private boolean hasValidFormat(final String number) {
        return number != null && number.matches("\\d{" + EXPECTED_LENGTH + "}");
    }

    private int[] toDigits(final String number) {
        return number.chars().map(c -> c - '0').toArray();
    }

    private boolean hasValidFirstDigit(final int[] digits) {
        return digits[0] >= MIN_FIRST_DIGIT;
    }

    private boolean hasValidCheckDigit(final int[] digits) {
        final int computed = computeCheckDigit(digits);
        return computed != MODULUS - 1 && computed == digits[EXPECTED_LENGTH - 1];
    }

    /**
     * Computes the control digit using the standard mod-11 algorithm.
     * Returns {@code MODULUS - 1} (i.e. 10) when the number is structurally invalid.
     */
    private int computeCheckDigit(final int[] digits) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += digits[i] * WEIGHTS[i];
        }
        final int remainder = sum % MODULUS;
        if (remainder == 0) {
            return 0;
        }
        return MODULUS - remainder;
    }
}
