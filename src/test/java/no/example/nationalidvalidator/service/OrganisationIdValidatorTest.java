package no.example.nationalidvalidator.service;

import static org.assertj.core.api.Assertions.assertThat;

import no.example.nationalidvalidator.model.IdType;
import no.example.nationalidvalidator.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("OrganizasjonsnummerValidator")
class OrganisationIdValidatorTest {

    private OrganisationIdValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OrganisationIdValidator();
    }

    // ---------------------------------------------------------------------------
    // Valid numbers
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Valid organization numbers")
    class ValidNumbers {

        @ParameterizedTest(name = "[{index}] {0} is valid")
        @ValueSource(strings = {
            "974760843",  // Skatteetaten
            "971032146",  // NAV
            "812345672"   // computed test number
        })
        @DisplayName("Known valid organization numbers are accepted")
        void validOrganizationNumbers(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.ORGANISASJONSNUMMER);
            assertThat(result.getMessage()).isEqualTo("Valid");
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — format
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Invalid organization numbers — format")
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
            "12345678",     // 8 digits — too short
            "1234567890",   // 10 digits — too long
            "97476084A",    // contains a letter
            "974 760 843"   // contains spaces
        })
        @DisplayName("Wrong length or non-numeric characters are rejected")
        void wrongFormatIsInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — first digit
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Invalid organization numbers — first digit")
    class InvalidFirstDigit {

        @ParameterizedTest(name = "[{index}] '{0}' has an invalid first digit")
        @ValueSource(strings = {
            "174760843",  // starts with 1
            "074760843",  // starts with 0
            "574760843"   // starts with 5
        })
        @DisplayName("Organization numbers not starting with 8 or 9 are rejected")
        void invalidFirstDigit(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — check digit
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Invalid organization numbers — check digit")
    class InvalidCheckDigit {

        @ParameterizedTest(name = "[{index}] '{0}' has an invalid check digit")
        @ValueSource(strings = {
            "974760844",  // correct is 3, not 4
            "971032140",  // correct is 6, not 0
            "812345671"   // correct is 2, not 1
        })
        @DisplayName("Wrong check digit is rejected")
        void wrongCheckDigitIsInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // Result structure
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Result structure")
    class ResultStructure {

        @Test
        @DisplayName("Valid number returns isValid=true and type ORGANISASJONSNUMMER")
        void validResultHasCorrectFields() {
            ValidationResult result = validator.validate("974760843");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.ORGANISASJONSNUMMER);
            assertThat(result.getMessage()).isEqualTo("Valid");
        }

        @Test
        @DisplayName("Invalid number returns isValid=false, null idType and a non-blank message")
        void invalidResultHasCorrectFields() {
            ValidationResult result = validator.validate("000000000");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getIdType()).isNull();
            assertThat(result.getMessage()).isNotBlank();
        }
    }
}
