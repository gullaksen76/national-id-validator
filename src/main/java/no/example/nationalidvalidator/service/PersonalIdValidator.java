package no.example.nationalidvalidator.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import no.example.nationalidvalidator.model.IdType;
import no.example.nationalidvalidator.model.ValidationResult;

/**
 * Validates Norwegian national identity numbers.
 *
 * <p>Supports regular fødselsnummer, D-numbers (day field + 40) and
 * H-numbers (month field + 40). The validator accepts both the current
 * control digit calculation and the 2032 calculation specified by Skatteetaten.
 *
 * <p>This class is stateless and thread-safe. A single instance can be shared freely.
 */
@Slf4j
public class PersonalIdValidator {

    private static final int EXPECTED_LENGTH = 11;
    private static final int MODULUS = 11;
    private static final int D_NUMBER_OFFSET = 40;
    private static final int H_NUMBER_OFFSET = 40;
    private static final int SYNTHETIC_MONTH_OFFSET = 80;
    private static final int TWO_DIGIT_YEAR_CENTURY = 2000;
    private static final int FIRST_D_NUMBER_DIGIT = 4;
    private static final int LAST_D_NUMBER_DIGIT = 7;
    private static final int H_NUMBER_MONTH_MAX = 52;
    private static final int SYNTHETIC_MONTH_MAX = 92;
    private static final int FIRST_CHECK_DIGIT_INDEX = 9;
    private static final int SECOND_CHECK_DIGIT_INDEX = 10;
    private static final int DAY_TENS_INDEX = 0;
    private static final int DAY_ONES_INDEX = 1;
    private static final int MONTH_TENS_INDEX = 2;
    private static final int MONTH_ONES_INDEX = 3;
    private static final int YEAR_TENS_INDEX = 4;
    private static final int YEAR_ONES_INDEX = 5;

    /** Valid k1 remainders in the 2032 control digit calculation. */
    private static final Set<Integer> VALID_K1_REMAINDERS = Set.of(0, 1, 2, 3);

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
        return validate(number, false);
    }

    /**
     * Validates a Norwegian national identity number or synthetic test number.
     *
     * @param number the 11-digit string to validate
     * @param synthetic whether the number uses the synthetic test-number date format
     * @return a {@link ValidationResult} describing whether the number is valid
     */
    public ValidationResult validate(final String number, final boolean synthetic) {
        log.debug("Validating national identity number");

        if (!hasValidFormat(number)) {
            return ValidationResult.invalid("Invalid format: must be exactly 11 digits");
        }

        final int[] digits = toDigits(number);

        if (!hasValidDate(digits, synthetic)) {
            return ValidationResult.invalid("Invalid date in number");
        }

        if (!hasValidCheckDigits(digits)) {
            return ValidationResult.invalid("Invalid check digits");
        }

        final IdType type = determineType(digits, synthetic);
        log.debug("National identity number is valid, type={}", type);
        return ValidationResult.valid(type);
    }

    private boolean hasValidFormat(final String number) {
        return number != null && number.matches("\\d{" + EXPECTED_LENGTH + "}");
    }

    private int[] toDigits(final String number) {
        return number.chars().map(c -> c - '0').toArray();
    }

    private boolean hasValidCheckDigits(final int[] digits) {
        return hasValidK1Remainder(digits) && hasValidK2Remainder(digits);
    }

    private boolean hasValidK1Remainder(final int[] digits) {
        final int remainder = computeRemainder(
                digits,
                K1_WEIGHTS,
                FIRST_CHECK_DIGIT_INDEX);
        return VALID_K1_REMAINDERS.contains(remainder);
    }

    private boolean hasValidK2Remainder(final int[] digits) {
        final int remainder = computeRemainder(
                digits,
                K2_WEIGHTS,
                SECOND_CHECK_DIGIT_INDEX);
        return remainder == 0;
    }

    private int computeRemainder(
            final int[] digits,
            final int[] weights,
            final int checkDigitIndex) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += digits[i] * weights[i];
        }
        return (sum + digits[checkDigitIndex]) % MODULUS;
    }

    private boolean hasValidDate(final int[] digits, final boolean synthetic) {
        int day = digits[DAY_TENS_INDEX] * 10 + digits[DAY_ONES_INDEX];
        int month = digits[MONTH_TENS_INDEX] * 10 + digits[MONTH_ONES_INDEX];
        final int year = TWO_DIGIT_YEAR_CENTURY
                + digits[YEAR_TENS_INDEX] * 10
                + digits[YEAR_ONES_INDEX];

        if (isDNumber(digits)) {
            day -= D_NUMBER_OFFSET;
        }

        if (synthetic) {
            if (!isSyntheticMonth(month)) {
                return false;
            }
            month -= SYNTHETIC_MONTH_OFFSET;
        } else if (isHNumberMonth(month)) {
            month -= H_NUMBER_OFFSET;
        }

        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (final DateTimeException e) {
            return false;
        }
    }

    private boolean isDNumber(final int[] digits) {
        return digits[0] >= FIRST_D_NUMBER_DIGIT && digits[0] <= LAST_D_NUMBER_DIGIT;
    }

    private boolean isHNumberMonth(final int month) {
        return month > H_NUMBER_OFFSET && month <= H_NUMBER_MONTH_MAX;
    }

    private boolean isSyntheticMonth(final int month) {
        return month > SYNTHETIC_MONTH_OFFSET && month <= SYNTHETIC_MONTH_MAX;
    }

    private IdType determineType(final int[] digits, final boolean synthetic) {
        final int month = digits[MONTH_TENS_INDEX] * 10 + digits[MONTH_ONES_INDEX];

        if (isDNumber(digits)) {
            return IdType.D_NUMBER;
        }
        if (!synthetic && isHNumberMonth(month)) {
            return IdType.H_NUMBER;
        }
        return IdType.FODSELSNUMMER;
    }
}
