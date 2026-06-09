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

@DisplayName("FodselsnummerValidator")
class FodselsnummerValidatorTest {

    private FodselsnummerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FodselsnummerValidator();
    }

    // ---------------------------------------------------------------------------
    // Valid numbers
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Gyldige ordinære fødselsnumre")
    class ValidOrdinaryNumbers {

        @ParameterizedTest(name = "[{index}] {0} er gyldig")
        @ValueSource(strings = {
            "01010112377",  // born 01.01.01, individ 123
            "15069012377",  // born 15.06.90, individ 123
            "01010099931",  // born 01.01.00, individ 999
            "31129900183"   // born 31.12.99, individ 001
        })
        @DisplayName("Gyldige fødselsnumre er gyldige og har type FODSELSNUMMER")
        void validOrdinaryNumbers(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.FODSELSNUMMER);
            assertThat(result.getMessage()).isEqualTo("Gyldig");
        }
    }

    @Nested
    @DisplayName("Gyldige D-numre")
    class ValidDNumbers {

        @Test
        @DisplayName("D-nummer (dag + 40) er gyldig og identifisert som D_NUMBER")
        void validDNumber() {
            // Born 05.08.1980, individ 300; day field = 05 + 40 = 45
            ValidationResult result = validator.validate("45088030013");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.D_NUMBER);
        }
    }

    @Nested
    @DisplayName("Gyldige H-numre")
    class ValidHNumbers {

        @Test
        @DisplayName("H-nummer (måned + 40) er gyldig og identifisert som H_NUMBER")
        void validHNumber() {
            // Born 15.01.1985, individ 200; month field = 01 + 40 = 41
            // k1 = 7, k2 = 5  →  number = "15418520075"
            ValidationResult result = validator.validate("15418520075");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isEqualTo(IdType.H_NUMBER);
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — format
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Ugyldige fødselsnumre — format")
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
            "1234567890",     // 10 siffer
            "123456789012",   // 12 siffer
            "0101011237A",    // bokstav i nummeret
            "           ",    // bare mellomrom
            "01 010 112 377"  // mellomrom i nummeret
        })
        @DisplayName("Feil lengde eller ikke-numeriske tegn gir ugyldig resultat")
        void wrongFormatIsInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — control digits
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Ugyldige fødselsnumre — kontrollsiffer")
    class InvalidCheckDigits {

        @ParameterizedTest(name = "[{index}] '{0}' har ugyldig kontrollsiffer")
        @ValueSource(strings = {
            "01010112345",  // k1 bør være 7, ikke 4
            "15069012370",  // k2 bør være 7, ikke 0
            "01010112300"   // begge kontrollsiffer feil
        })
        @DisplayName("Galt kontrollsiffer gir ugyldig resultat")
        void wrongCheckDigitsAreInvalid(String number) {
            ValidationResult result = validator.validate(number);

            assertThat(result.isValid()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // Invalid numbers — date
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Ugyldige fødselsnumre — dato")
    class InvalidDate {

        @Test
        @DisplayName("Alle nuller gir ugyldig dato (dag 0 og måned 0)")
        void allZerosIsInvalidDate() {
            // "00000000000" har k1=k2=0 (passerer kontrollsiffer), men dag=0 er ugyldig
            ValidationResult result = validator.validate("00000000000");

            assertThat(result.isValid()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // Result structure
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("Resultatstruktur")
    class ResultStructure {

        @Test
        @DisplayName("Gyldig nummer returnerer isValid=true, idType satt og melding 'Gyldig'")
        void validResultHasCorrectFields() {
            ValidationResult result = validator.validate("01010112377");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getIdType()).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Gyldig");
        }

        @Test
        @DisplayName("Ugyldig nummer returnerer isValid=false, idType null og en feilmelding")
        void invalidResultHasCorrectFields() {
            ValidationResult result = validator.validate("12345678901");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getIdType()).isNull();
            assertThat(result.getMessage()).isNotBlank();
        }
    }
}
