package no.example.nationalidvalidator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.example.nationalidvalidator.dto.ValidationRequest;
import no.example.nationalidvalidator.dto.ValidationResponse;
import no.example.nationalidvalidator.model.ValidationResult;
import no.example.nationalidvalidator.service.FodselsnummerValidator;
import no.example.nationalidvalidator.service.OrganizasjonsnummerValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller exposing endpoints for Norwegian identifier validation. */
@Slf4j
@RestController
@RequestMapping("/api/v1/validate")
@RequiredArgsConstructor
public class ValidationController {

    private final FodselsnummerValidator fodselsnummerValidator;
    private final OrganizasjonsnummerValidator organizasjonsnummerValidator;

    /**
     * Validates a Norwegian national identity number (fødselsnummer).
     *
     * @param request body containing the number to validate
     * @return 200 OK with a validation result
     */
    @PostMapping("/fodselsnummer")
    public ResponseEntity<ValidationResponse> validateFodselsnummer(
            @Valid @RequestBody final ValidationRequest request) {
        log.info("Received request to validate fødselsnummer");
        final ValidationResult result = fodselsnummerValidator.validate(request.getNumber());
        return ResponseEntity.ok(toResponse(request.getNumber(), result));
    }

    /**
     * Validates a Norwegian organization number (organisasjonsnummer).
     *
     * @param request body containing the number to validate
     * @return 200 OK with a validation result
     */
    @PostMapping("/organisasjonsnummer")
    public ResponseEntity<ValidationResponse> validateOrganizasjonsnummer(
            @Valid @RequestBody final ValidationRequest request) {
        log.info("Received request to validate organisasjonsnummer");
        final ValidationResult result =
                organizasjonsnummerValidator.validate(request.getNumber());
        return ResponseEntity.ok(toResponse(request.getNumber(), result));
    }

    private ValidationResponse toResponse(final String number, final ValidationResult result) {
        return ValidationResponse.builder()
                .number(number)
                .valid(result.isValid())
                .idType(result.getIdType() != null ? result.getIdType().name() : null)
                .message(result.getMessage())
                .build();
    }
}
