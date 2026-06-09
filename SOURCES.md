# Data Sources and Specifications

This document outlines the official sources and specifications used in this validator.

## Personal Identity Numbers (Fødselsnummer)

### Official Specification
- **Source:** Skatteetaten (Norwegian Tax Administration)
- **URL:** https://www.skatteetaten.no/person/folkeregister/om-fonnummeret/
- **Coverage:**
  - Structure and validation rules for ordinary fødselsnummer
  - D-number specification (day field + 40)
  - H-number specification (month field + 40)
  - Control digit calculation (legacy mod-11 algorithm)

### 2032 Control Digit Specification
- **Source:** Skatteetaten (2032 PID modernization)
- **Details:** Updated control digit calculation allowing more flexible remainders
  - Legacy: k1_remainder must produce valid digit (0-9, not 10)
  - 2032: k1_remainder ∈ {0, 1, 2, 3} (more flexible)
  - Both regimes validated for compatibility

## Organization Numbers (Organisasjonsnummer)

### Official Specification
- **Source:** Brønnøysundregistrene (Norwegian National Registry of Enterprises)
- **URL:** https://www.brreg.no/om-oss/registrene-vare/om-enhetsregisteret/organisasjonsnummeret/
- **Specification Details:**
  - **Structure:** 9 digits
  - **Control Digit:** Last digit calculated using mod-11
  - **Formula:** `k = 11 − (weighted_sum mod 11)`
  - **Weights:** 3, 2, 7, 6, 5, 4, 3, 2 (applied left to right)
  - **Invalid Result:** If formula produces 10, the number is structurally invalid
  - **Zero Result:** If formula produces 11, it becomes 0

### First Digit Convention
- **Note:** Brønnøysundregistrene does NOT explicitly specify first digit requirements
- **Practice:** Norwegian organization numbers conventionally use 8 or 9 as the first digit
  - This is enforced in this validator for practical purposes
  - May be relaxed in future if requirements change
- **Justification:** Identifies entity type in Norwegian registry systems

## Implementation Notes

1. **Brønnøysundregistrene specification is the primary source** for organization number validation
2. **Skatteetaten specification is the primary source** for personal identity number validation
3. **Test data** comes from official examples where available
4. **Control digit logic** implemented exactly as specified in official documents
5. **Error messages** designed to help identify validation failure points

## Test Data Sources

### Finding Valid and Invalid Test Numbers

Skatteetaten does not publish an official public dataset of test fødselsnummer.
However, the following resources may contain useful information:

1. **Altinn** — Norwegian Government's digital services platform
   - URL: https://www.altinn.no/
   - May contain API documentation with test examples

2. **Skatteetaten API Documentation**
   - Contact: Skatteetaten's developer support
   - May provide test datasets for their services

3. **Direktoratet for e-helse** — Norwegian Health Authority
   - Maintains health-related test data
   - May include test fødselsnummer for health systems

4. **Folkeregisteret** — Central Population Register
   - May provide testing guidelines
   - Contact: Skatteetaten directly

### Best Practices for Test Data

- **Generate programmatically**: Use valid control digit algorithm to generate test numbers
- **Use synthetic numbers**: Numbers outside normal date ranges (year 2025+) are unlikely to be real
- **D-numbers and H-numbers**: Safe for testing as they're administratively assigned
- **Anonymous data**: Any actual fødselsnummer in production code should be treated as PII

### In This Project

Test data in `PersonalIdValidatorTest.java` includes:
- Valid ordinary fødselsnummer (public test data)
- Valid D-numbers
- Valid H-numbers
- Skatteetaten 2032 specification examples
- Invalid format/structure/checksum cases

If you find official Skatteetaten test data resources, please update this document.

## Future Updates

If official specifications change, this document and the corresponding validation logic will be updated.
Source links are included in code comments and README for easy verification.
