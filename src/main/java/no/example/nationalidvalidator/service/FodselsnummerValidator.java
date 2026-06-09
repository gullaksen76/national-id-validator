package no.example.nationalidvalidator.service;

import lombok.extern.slf4j.Slf4j;
import no.example.nationalidvalidator.model.IdType;
import no.example.nationalidvalidator.model.ValidationResult;
import org.springframework.stereotype.Service;

/**
 * Validates Norwegian national identity numbers (fødselsnummer).
 *
 * <p>Supports regular fødselsnummer, D-numbers (day field + 40) and
 * H-numbers (month field + 40). The algorithm is specified by Skatteetaten.
 */
@Slf4j
@Service
public class FodselsnummerValidator {

    private static final int EXPECTED_LENGTH = 11;
    private static final int MODULUS = 11;
    private static final int D_NUMBER_OFFSET = 40;
    private static final int H_NUMBER_OFFSET = 40;
    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 31;
    private static final int MIN_MONTH = 1;
    private static final int MAX_MONTH = 12;
    private static final int D_NUMBER_DAY_MAX = 71;
    private static final int H_NUMBER_MONTH_MAX = 52;

    /** Weights used to compute the first control digit (k1). */
    private static final int[] K1_WEIGHTS = {3, 7, 6, 1, 8, 9, 4, 5, 2};

    /** Weights used to compute the second control digit (k2). */
    private static final int[] K2_WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    /**
     * Validates a Norwegian national identity number.
     *
     * @param number the 11-digit string to validate
     * @return a {@link ValidationResult} describing whether the number is valid
     */
    public ValidationResult validate(final String number) {
        log.debug("Validating fødselsnummer: {}", number);

        if (!hasValidFormat(number)) {
            return ValidationResult.invalid("Ugyldig format: må bestå av nøyaktig 11 siffer");
        }

        final int[] digits = toDigits(number);

        if (!hasValidCheckDigits(digits)) {
            return ValidationResult.invalid("Ugyldig kontrollsiffer");
        }

        if (!hasValidDate(digits)) {
            return ValidationResult.invalid("Ugyldig dato i nummeret");
        }

        final IdType type = determineType(digits);
        log.debug("Fødselsnummer {} is valid, type={}", number, type);
        return ValidationResult.valid(type);
    }

    private boolean hasValidFormat(final String number) {
        return number != null && number.matches("\\d{" + EXPECTED_LENGTH + "}");
    }

    private int[] toDigits(final String number) {
        return number.chars().map(c -> c - '0').toArray();
    }

    private boolean hasValidCheckDigits(final int[] digits) {
        final int k1 = computeCheckDigit(digits, K1_WEIGHTS);
        if (k1 == MODULUS - 1 || k1 != digits[9]) {
            return false;
        }
        final int k2 = computeCheckDigit(digits, K2_WEIGHTS);
        return k2 != MODULUS - 1 && k2 == digits[10];
    }

    /**
     * Computes a control digit using the standard mod-11 algorithm.
     * Returns {@code MODULUS - 1} (i.e. 10) when the number is structurally invalid.
     */
    private int computeCheckDigit(final int[] digits, final int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += digits[i] * weights[i];
        }
        final int remainder = sum % MODULUS;
        if (remainder == 0) {
            return 0;
        }
        return MODULUS - remainder;
    }

    private boolean hasValidDate(final int[] digits) {
        int day = digits[0] * 10 + digits[1];
        int month = digits[2] * 10 + digits[3];

        if (day > D_NUMBER_OFFSET && day <= D_NUMBER_DAY_MAX) {
            day -= D_NUMBER_OFFSET;
        }
        if (month > H_NUMBER_OFFSET && month <= H_NUMBER_MONTH_MAX) {
            month -= H_NUMBER_OFFSET;
        }

        return day >= MIN_DAY && day <= MAX_DAY
                && month >= MIN_MONTH && month <= MAX_MONTH;
    }

    private IdType determineType(final int[] digits) {
        final int day = digits[0] * 10 + digits[1];
        final int month = digits[2] * 10 + digits[3];

        if (day > D_NUMBER_OFFSET) {
            return IdType.D_NUMBER;
        }
        if (month > H_NUMBER_OFFSET) {
            return IdType.H_NUMBER;
        }
        return IdType.FODSELSNUMMER;
    }
}
