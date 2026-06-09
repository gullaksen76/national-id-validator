# national-id-validator

A lightweight, plain Java library for validating Norwegian national identity numbers
(fødselsnummer) and organization numbers (organisasjonsnummer).

## Features

- **Fødselsnummer** — standard 11-digit personal identity number
- **D-number** — assigned to foreign nationals (day field + 40)
- **H-number** — assigned to patients without a valid fødselsnummer (month field + 40)
- **Organization number** — 9-digit number issued by Brønnøysundregistrene
- Stateless, thread-safe validators — safe to share as singletons
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

### National identity number (fødselsnummer)

```java
FodselsnummerValidator validator = new FodselsnummerValidator();

ValidationResult result = validator.validate("01010112377");

result.isValid();       // true
result.getIdType();     // IdType.FODSELSNUMMER
result.getMessage();    // "Valid"

// D-number
ValidationResult dResult = validator.validate("45088030013");
dResult.getIdType();    // IdType.D_NUMBER

// H-number
ValidationResult hResult = validator.validate("15418520075");
hResult.getIdType();    // IdType.H_NUMBER

// Invalid
ValidationResult invalid = validator.validate("12345678901");
invalid.isValid();      // false
invalid.getMessage();   // human-readable error message
```

### Organization number (organisasjonsnummer)

```java
OrganizasjonsnummerValidator validator = new OrganizasjonsnummerValidator();

ValidationResult result = validator.validate("974760843");

result.isValid();       // true
result.getIdType();     // IdType.ORGANISASJONSNUMMER
```

### Spring applications

Since the validators are plain Java classes they can be registered as beans with no
additional configuration:

```java
@Configuration
public class ValidatorConfig {

    @Bean
    public FodselsnummerValidator fodselsnummerValidator() {
        return new FodselsnummerValidator();
    }

    @Bean
    public OrganizasjonsnummerValidator organizasjonsnummerValidator() {
        return new OrganizasjonsnummerValidator();
    }
}
```

## Algorithm

### National identity number (11 digits: DDMMYYIIIKK)

Control digits are computed using a weighted mod-11 sum:

| Control digit | Weights |
|---|---|
| k1 (position 10) | 3 7 6 1 8 9 4 5 2 |
| k2 (position 11) | 5 4 3 2 7 6 5 4 3 2 |

Result `= 11 − (sum mod 11)`. Result `11 → 0`. Result `10 → structurally invalid combination`.

**D-number:** day field increased by 40 (day 1–31 → 41–71).  
**H-number:** month field increased by 40 (month 1–12 → 41–52).

### Organization number (9 digits)

The first digit must be 8 or 9. The control digit (position 9) is computed using weights
`3 2 7 6 5 4 3 2` over the first eight digits, using the same mod-11 formula as above.

## Building

```bash
# Compile and run all tests with Checkstyle and SpotBugs
mvn verify

# Tests only
mvn test

# Checkstyle + SpotBugs without running tests
mvn validate spotbugs:check
```

## License

[Apache 2.0](LICENSE)
