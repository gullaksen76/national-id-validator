# national-id-validator

A lightweight, plain Java library for validating Norwegian national identity numbers
(fødselsnummer) and organization numbers (organisasjonsnummer). Supports both legacy
and 2032 control digit calculation standards as defined by Skatteetaten.

## Features

### Personal Identity Numbers (fødselsnummer)

- **Ordinary fødselsnummer** — standard 11-digit personal identity number
- **D-number** — assigned to foreign nationals (day field + 40)
- **H-number** — assigned to patients without a valid fødselsnummer (month field + 40)
- **2032 control digit support** — validates against both legacy mod-11 and modern 2032 standards
- **Synthetic test numbers** — special date format for test purposes

### Organization Numbers (organisasjonsnummer)

- **9-digit format** — issued by Brønnøysundregistrene
- **First digit validation** — must be 8 or 9

### Common Features

- Stateless, thread-safe validators — safe to share as singletons
- Detailed validation breakdown — understand *why* a number failed
- SLF4J for logging — bring your own implementation

## Requirements

- Java 21+
- SLF4J implementation on the classpath (provided by your application)

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>no.example</groupId>
    <artifactId>national-id-validator</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Usage

### Basic Validation

```java
PersonalIdValidator validator = new PersonalIdValidator();

ValidationResult result = validator.validate("01010112377");

result.isValid();       // true
result.getIdType();     // IdType.FODSELSNUMMER
result.getMessage();    // "Valid"

// D-number
ValidationResult dResult = validator.validate("45088030013");
dResult.getIdType();    // IdType.D_NUMBER

// Invalid number
ValidationResult invalid = validator.validate("12345678901");
invalid.isValid();      // false
invalid.getMessage();   // "Invalid check digits"
```

### Detailed Validation

Get a granular breakdown of what passed and what failed:

```java
PersonalIdValidator validator = new PersonalIdValidator();

ValidationDetails details = validator.validateDetails("01010112377");

details.isElevenDigits();              // true
details.isNumericOnly();               // true
details.getIdType();                   // IdType.FODSELSNUMMER
details.isValidStructure();            // true
details.getControlDigitRegime();       // ControlDigitRegime.LEGACY

// If invalid, get details on what failed:
ValidationDetails invalid = validator.validateDetails("12345678");
invalid.isElevenDigits();              // false
invalid.isNumericOnly();               // true
invalid.getIdType();                   // null
invalid.isValidStructure();            // false
invalid.getControlDigitRegime();       // ControlDigitRegime.NONE
invalid.getErrorMessage();             // "Invalid format: must be exactly 11 digits"
```

### Control Digit Regimes

A number can validate under one or both regimes:

```java
PersonalIdValidator validator = new PersonalIdValidator();

ValidationDetails details = validator.validateDetails("02013299997");
details.getControlDigitRegime();       // ControlDigitRegime.PID_2032 (2032 only)

ValidationDetails details2 = validator.validateDetails("01010112377");
details2.getControlDigitRegime();      // ControlDigitRegime.LEGACY (legacy only)

ValidationDetails details3 = validator.validateDetails("some_number");
details3.getControlDigitRegime();      // ControlDigitRegime.BOTH (validates in both)
```

### Organization Numbers

```java
OrganisationIdValidator validator = new OrganisationIdValidator();

ValidationResult result = validator.validate("974760843");

result.isValid();       // true
result.getIdType();     // IdType.ORGANISASJONSNUMMER
```

## Validation Rules

### Personal Identity Number (11 digits: DDMMYYIIIKK)

The validator checks:

1. **Length** — exactly 11 digits
2. **Numeric only** — all characters must be digits
3. **Type identification** — ordinary fødselsnummer, D-number, or H-number
4. **Date validity** — D/M/Y must form a valid date
5. **Control digits** — K1 and K2 must match the input, using one or both control regimes

**Control digit regimes:**
- **Legacy (mod-11):** `k = 11 − (sum mod 11)`. Valid results: 0–9. Result 10 is invalid.
- **2032 (PID):** `k1_remainder ∈ {0, 1, 2, 3}`, `k2_remainder = 0`. More flexible than legacy.

Weights for legacy calculation:
- K1: `3 7 6 1 8 9 4 5 2`
- K2: `5 4 3 2 7 6 5 4 3 2`

### Organization Number (9 digits)

1. **Length** — exactly 9 digits
2. **Numeric only** — all characters must be digits
3. **First digit** — must be 8 or 9
4. **Control digit** — position 9 uses weights `3 2 7 6 5 4 3 2` with mod-11

## Building

```bash
# Compile, run tests, Checkstyle, and SpotBugs
mvn verify

# Tests only
mvn test

# Static analysis only (no tests)
mvn validate spotbugs:check
```

## Spring Integration

```java
@Configuration
public class ValidatorConfig {

    @Bean
    public PersonalIdValidator personalIdValidator() {
        return new PersonalIdValidator();
    }

    @Bean
    public OrganisationIdValidator organisationIdValidator() {
        return new OrganisationIdValidator();
    }
}
```

## References

- Skatteetaten: [Fødselsnummeret](https://www.skatteetaten.no/person/folkeregister/om-fonnummeret/)
- Brønnøysundregistrene: [Organisasjonsnummeret](https://www.brreg.no/om-oss/oppgaver-og-organisasjon/nasjonale-registre-og-databaser/om-organisasjonsregisteret/)

## License

[Apache 2.0](LICENSE)
