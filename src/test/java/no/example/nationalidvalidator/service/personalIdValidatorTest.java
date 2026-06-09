package no.example.nationalidvalidator.service;

import static org.assertj.core.api.Assertions.assertThat;

import no.example.nationalidvalidator.model.ControlDigitRegime;
import no.example.nationalidvalidator.model.IdType;
import no.example.nationalidvalidator.model.ValidationDetails;
import no.example.nationalidvalidator.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PersonalIdValidator")
class PersonalIdValidatorTest {

    private PersonalIdValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PersonalIdValidator();
    }

    // ---------------------------------------------------------------------------
    // Valid numbers
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Valid ordinary national identity numbers")
    class ValidOrdinaryNumbers {

        @ParameterizedTest(name = "[{index}] {0} is valid")
        @ValueSource(strings = {
            "01010112377",  // born 01.01.01, individual number 123
            "15069012377",  // born 15.06.90, individual number 123
            "01010099931",  // born 01.01.00, individual number 999
            "31129900183",  // born 31.12.99, individual number 001
            "15068505094",  // generated: born 15.06.85, individual 050 (legacy)
            "22127512357"   // generated: born 22.12.75, individual 123 (legacy)
        })
        @DisplayName("Valid national identity numbers are accepted with type FODSELSNUMMER")
        void validOrdinaryNumbers(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.FODSELSNUMMER);
            assertThat(result.getMessage()).isEqualTo("Valid");
        }
    }

    @Nested
    @DisplayName("Valid D-numbers")
    class ValidDNumbers {

        @Test
        @DisplayName("D-number (day + 40) is accepted and identified as D_NUMBER")
        void validDNumber() {
            // Born 05.08.1980, individual number 300; day field = 05 + 40 = 45
            ValidationResult result = validator.validate("45088030013");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.D_NUMBER);
        }
    }

    @Nested
    @DisplayName("Valid 2032 control digit calculation")
    class Valid2032ControlDigitCalculation {

        @ParameterizedTest(name = "[{index}] {0} is valid")
        @ValueSource(strings = {
            "02013299997",  // Skatteetaten example
            "30108299920",  // Skatteetaten example
            "30108299939",  // Skatteetaten example
            "10033320014",  // generated: born 10.03.33, individual 200
            "05114050050"   // generated: born 05.11.40, individual 500
        })
        @DisplayName("Skatteetaten 2032 examples and generated 2032+ numbers are accepted")
        void skatteetaten2032ExamplesAreAccepted(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.FODSELSNUMMER);
        }
    }

    @Nested
    @DisplayName("Valid H-numbers")
    class ValidHNumbers {

        @Test
        @DisplayName("H-number (month + 40) is accepted and identified as H_NUMBER")
        void validHNumber() {
            // Born 15.01.1985, individual number 200; month field = 01 + 40 = 41
            // k1 = 7, k2 = 5  ->  number = "15418520075"
            ValidationResult result = validator.validate("15418520075");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.H_NUMBER);
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — format
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Invalid national identity numbers — format")
    class InvalidFormat {

        @ParameterizedTest(name = "[{index}] null/empty string is invalid")
        @NullAndEmptySource
        @DisplayName("Null and empty string are rejected")
        void nullAndEmptyAreInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }

        @ParameterizedTest(name = "[{index}] '{0}' has invalid format")
        @ValueSource(strings = {
            "1234567890",     // 10 digits — too short
            "123456789012",   // 12 digits — too long
            "0101011237A",    // contains a letter
            "           ",    // whitespace only
            "01 010 112 377"  // contains spaces
        })
        @DisplayName("Wrong length or non-numeric characters are rejected")
        void wrongFormatIsInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — check digits
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Invalid national identity numbers — check digits")
    class InvalidCheckDigits {

        @ParameterizedTest(name = "[{index}] '{0}' has invalid check digits")
        @ValueSource(strings = {
            "01010112345",  // k1 should be 7, not 4
            "15069012370",  // k2 should be 7, not 0
            "01010112300"   // both check digits wrong
        })
        @DisplayName("Wrong check digits are rejected")
        void wrongCheckDigitsAreInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — date
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Invalid national identity numbers — date")
    class InvalidDate {

        @Test
        @DisplayName("All-zeros number is rejected due to invalid date (day 0, month 0)")
        void allZerosIsInvalidDate() {
            // "00000000000" passes check digit validation (k1=k2=0) but day=0 is invalid
            ValidationResult result = validator.validate("00000000000");

            assertThat(result.isValid()).isFalse();
        }

        @ParameterizedTest(name = "[{index}] '{0}' has an invalid calendar date")
        @ValueSource(strings = {
            "31029912370",
            "31043112344"
        })
        @DisplayName("Calendar-invalid dates are rejected")
        void invalidCalendarDatesAreRejected(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Invalid date in number");
        }
    }

    // ---------------------------------------------------------------------------
    // Result structure
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Result structure")
    class ResultStructure {

        @Test
        @DisplayName("Valid number returns isValid=true, non-null idType and message 'Valid'")
        void validResultHasCorrectFields() {
            ValidationResult result = validator.validate("01010112377");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Valid");
        }

        @Test
        @DisplayName("Invalid number returns isValid=false, null idType and a non-blank message")
        void invalidResultHasCorrectFields() {
            ValidationResult result = validator.validate("12345678901");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getIdType()).isNull();
            assertThat(result.getMessage()).isNotBlank();
        }
    }

    // ---------------------------------------------------------------------------
    // Detailed validation
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Detailed validation")
    class DetailedValidation {

        @Test
        @DisplayName("Null input returns all-false details")
        void nullInputReturnsInvalidDetails() {
            ValidationDetails details = validator.validateDetails(null);

            assertThat(details.isElevenDigits()).isFalse();
            assertThat(details.isNumericOnly()).isFalse();
            assertThat(details.getIdType()).isNull();
            assertThat(details.isValidStructure()).isFalse();
            assertThat(details.getControlDigitRegime()).isEqualTo(ControlDigitRegime.NONE);
            assertThat(details.isValid()).isFalse();
        }

        @Test
        @DisplayName("Valid number with both legacy and 2032 validation")
        void validNumberWithBothRegimes() {
            ValidationDetails details = validator.validateDetails("01010112377");

            assertThat(details.isElevenDigits()).isTrue();
            assertThat(details.isNumericOnly()).isTrue();
            assertThat(details.getIdType()).isEqualTo(IdType.FODSELSNUMMER);
            assertThat(details.isValidStructure()).isTrue();
            // This number validates under both legacy and 2032 regimes
            assertThat(details.getControlDigitRegime()).isIn(
                    ControlDigitRegime.LEGACY,
                    ControlDigitRegime.BOTH);
            assertThat(details.isValid()).isTrue();
            assertThat(details.getErrorMessage()).isEmpty();
        }

        @Test
        @DisplayName("Valid 2032 PID number is identified correctly")
        void valid2032NumberIsValid() {
            ValidationDetails details = validator.validateDetails("02013299997");

            assertThat(details.isElevenDigits()).isTrue();
            assertThat(details.isNumericOnly()).isTrue();
            assertThat(details.getIdType()).isEqualTo(IdType.FODSELSNUMMER);
            assertThat(details.isValidStructure()).isTrue();
            // 2032 examples validate under their regime
            assertThat(details.getControlDigitRegime()).isIn(
                    ControlDigitRegime.PID_2032,
                    ControlDigitRegime.BOTH);
            assertThat(details.isValid()).isTrue();
        }

        @ParameterizedTest(name = "[{index}] Invalid input: {0}")
        @ValueSource(strings = {
            "1234567890",      // 10 digits
            "123456789012",    // 12 digits
            "0101011237A",     // non-numeric
            "12345678901"      // 11 digits but invalid structure/checksum
        })
        @DisplayName("Invalid formats are detected")
        void invalidFormatsDetected(String number) {
            ValidationDetails details = validator.validateDetails(number);

            assertThat(details.isValid()).isFalse();
            assertThat(details.getErrorMessage()).isNotBlank();
        }

        @Test
        @DisplayName("Invalid structure gets correct error message")
        void invalidStructureGetsErrorMessage() {
            ValidationDetails details = validator.validateDetails("00000000000");

            assertThat(details.isElevenDigits()).isTrue();
            assertThat(details.isNumericOnly()).isTrue();
            assertThat(details.isValidStructure()).isFalse();
            assertThat(details.getErrorMessage()).isEqualTo("Invalid date in number");
        }

        @Test
        @DisplayName("Invalid check digits gets correct error message")
        void invalidCheckDigitsGetsErrorMessage() {
            ValidationDetails details = validator.validateDetails("01010112345");

            assertThat(details.isElevenDigits()).isTrue();
            assertThat(details.isNumericOnly()).isTrue();
            assertThat(details.isValidStructure()).isTrue();
            assertThat(details.getControlDigitRegime()).isEqualTo(ControlDigitRegime.NONE);
            assertThat(details.getErrorMessage()).isEqualTo("Invalid check digits");
        }
    }
}
