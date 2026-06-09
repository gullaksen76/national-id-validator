package no.example.nationalidvalidator.dto;

import lombok.Builder;
import lombok.Value;

/** Response body returned by all validation endpoints. */
@Value
@Builder
public class ValidationResponse {

    private String number;
    private boolean valid;
    private String idType;
    private String message;
}
