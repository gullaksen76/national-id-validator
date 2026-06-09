# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0-SNAPSHOT] - 2026-06-09

### Added
- `FodselsnummerValidator` — validates Norwegian national identity numbers using
  the weighted mod-11 algorithm specified by Skatteetaten
  - Supports ordinary fødselsnummer
  - Supports D-nummer (day field + 40, assigned to foreign nationals)
  - Supports H-nummer (month field + 40, assigned to patients)
- `OrganizasjonsnummerValidator` — validates Norwegian organization numbers
  using the weighted mod-11 algorithm specified by Brønnøysundregistrene
  - Enforces that the first digit must be 8 or 9
- `ValidationResult` — immutable result type carrying `valid`, `idType` and `message`
- `IdType` enum — `FODSELSNUMMER`, `D_NUMBER`, `H_NUMBER`, `ORGANISASJONSNUMMER`
- Checkstyle enforcement (custom ruleset with Lombok-compatible suppressions)
- SpotBugs static analysis (`effort=Max`, `threshold=Medium`)
- JUnit 5 + AssertJ test suite covering valid numbers, format errors,
  control-digit errors and date errors for both validator types

[Unreleased]: https://github.com/gullaksen76/national-id-validator/compare/v1.0.0-SNAPSHOT...HEAD
[1.0.0-SNAPSHOT]: https://github.com/gullaksen76/national-id-validator/releases/tag/v1.0.0-SNAPSHOT
