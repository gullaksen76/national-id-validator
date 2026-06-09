# national-id-validator

A lightweight, dependency-free Java library for validating Norwegian national identity numbers
(fødselsnummer) and organization numbers (organisasjonsnummer).

## Features

- **Fødselsnummer** — standard 11-digit personal identity number
- **D-nummer** — assigned to foreign nationals (day field + 40)
- **H-nummer** — assigned to patients without a valid fødselsnummer (month field + 40)
- **Organisasjonsnummer** — 9-digit organization number (Brønnøysundregistrene)
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

### Fødselsnummer

```java
FodselsnummerValidator validator = new FodselsnummerValidator();

ValidationResult result = validator.validate("01010112377");

result.isValid();          // true
result.getIdType();        // IdType.FODSELSNUMMER
result.getMessage();       // "Gyldig"

// D-nummer
ValidationResult dResult = validator.validate("45088030013");
dResult.getIdType();       // IdType.D_NUMBER

// H-nummer
ValidationResult hResult = validator.validate("15418520075");
hResult.getIdType();       // IdType.H_NUMBER

// Ugyldig
ValidationResult invalid = validator.validate("12345678901");
invalid.isValid();         // false
invalid.getMessage();      // human-readable error message
```

### Organisasjonsnummer

```java
OrganizasjonsnummerValidator validator = new OrganizasjonsnummerValidator();

ValidationResult result = validator.validate("974760843");

result.isValid();          // true
result.getIdType();        // IdType.ORGANISASJONSNUMMER
```

### Spring-applikasjoner

Siden validatorene er rene Java-klasser kan de enkelt registreres som beans:

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

## Algoritme

### Fødselsnummer (11 siffer: DDMMYYIIIKK)

Kontrollsifrene beregnes med vektet mod-11:

| Kontrollsiffer | Vekter |
|---|---|
| k1 (pos. 10) | 3 7 6 1 8 9 4 5 2 |
| k2 (pos. 11) | 5 4 3 2 7 6 5 4 3 2 |

Resultat `= 11 − (sum mod 11)`. Resultat `11 → 0`. Resultat `10 → ugyldig kombinasjon`.

**D-nummer:** dag-feltet økt med 40 (dag 1–31 → 41–71).  
**H-nummer:** måned-feltet økt med 40 (måned 1–12 → 41–52).

### Organisasjonsnummer (9 siffer)

Første siffer må være 8 eller 9. Kontrollsifferet (pos. 9) beregnes med vekter `3 2 7 6 5 4 3 2`
over de åtte første sifrene, samme mod-11-formel som over.

## Building

```bash
# Kompiler og kjør alle tester
mvn verify

# Bare tester
mvn test

# Checkstyle + SpotBugs uten tester
mvn validate spotbugs:check
```

## License

[Apache 2.0](LICENSE)
