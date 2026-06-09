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
class OrganizasjonsnummerValidatorTest {

    private OrganizasjonsnummerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OrganizasjonsnummerValidator();
    }

    @Nested
    @DisplayName("Gyldige organisasjonsnumre")
    class ValidNumbers {

        @ParameterizedTest(name = "[{index}] {0} er gyldig")
        @ValueSource(strings = {
            "974760843",  // Skatteetaten
            "971032146",  // NAV
            "812345672"   // beregnet testnummer
        })
        @DisplayName("Kjente gyldige organisasjonsnumre er gyldige")
        void validOrganizationNumbers(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.ORGANISASJONSNUMMER);
            assertThat(result.getMessage()).isEqualTo("Gyldig");
        }
    }

    @Nested
    @DisplayName("Ugyldige organisasjonsnumre — format")
    class InvalidFormat {

        @ParameterizedTest(name = "[{index}] null/tom streng er ugyldig")
        @NullAndEmptySource
        @DisplayName("Null og tom streng er ugyldig")
        void nullAndEmptyAreInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }

        @ParameterizedTest(name = "[{index}] '{0}' er ugyldig format")
        @ValueSource(strings = {
            "12345678",     // 8 siffer — for kort
            "1234567890",   // 10 siffer — for langt
            "97476084A",    // bokstav
            "974 760 843"   // mellomrom
        })
        @DisplayName("Feil lengde eller ikke-numeriske tegn gir ugyldig resultat")
        void wrongFormatIsInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Ugyldige organisasjonsnumre — første siffer")
    class InvalidFirstDigit {

        @ParameterizedTest(name = "[{index}] '{0}' har ugyldig første siffer")
        @ValueSource(strings = {
            "174760843",  // starter med 1
            "074760843",  // starter med 0
            "574760843"   // starter med 5
        })
        @DisplayName("Organisasjonsnummer som ikke starter med 8 eller 9 er ugyldig")
        void invalidFirstDigit(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Ugyldige organisasjonsnumre — kontrollsiffer")
    class InvalidCheckDigit {

        @ParameterizedTest(name = "[{index}] '{0}' har ugyldig kontrollsiffer")
        @ValueSource(strings = {
            "974760844",  // korrekt er 3, ikke 4
            "971032140",  // korrekt er 6, ikke 0
            "812345671"   // korrekt er 2, ikke 1
        })
        @DisplayName("Galt kontrollsiffer gir ugyldig resultat")
        void wrongCheckDigitIsInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Resultatstruktur")
    class ResultStructure {

        @Test
        @DisplayName("Gyldig nummer har isValid=true og type ORGANISASJONSNUMMER")
        void validResultHasCorrectFields() {
            ValidationResult result = validator.validate("974760843");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.ORGANISASJONSNUMMER);
            assertThat(result.getMessage()).isEqualTo("Gyldig");
        }

        @Test
        @DisplayName("Ugyldig nummer har isValid=false, null idType og en feilmelding")
        void invalidResultHasCorrectFields() {
            ValidationResult result = validator.validate("000000000");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getIdType()).isNull();
            assertThat(result.getMessage()).isNotBlank();
        }
    }
}
