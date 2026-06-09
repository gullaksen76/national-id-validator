package no.example.nationalidvalidator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body carrying the identifier string to be validated. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {

    @NotBlank(message = "Nummer kan ikke være tomt")
    private String number;
}
