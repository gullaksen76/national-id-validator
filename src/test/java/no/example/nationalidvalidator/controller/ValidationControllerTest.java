package no.example.nationalidvalidator.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import no.example.nationalidvalidator.model.IdType;
import no.example.nationalidvalidator.model.ValidationResult;
import no.example.nationalidvalidator.service.FodselsnummerValidator;
import no.example.nationalidvalidator.service.OrganizasjonsnummerValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ValidationController.class)
@DisplayName("ValidationController")
class ValidationControllerTest {

    private static final String FODSELSNUMMER_URL = "/api/v1/validate/fodselsnummer";
    private static final String ORG_URL = "/api/v1/validate/organisasjonsnummer";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FodselsnummerValidator fodselsnummerValidator;

    @MockitoBean
    private OrganizasjonsnummerValidator organizasjonsnummerValidator;

    @Nested
    @DisplayName("POST /api/v1/validate/fodselsnummer")
    class FodselsnummerEndpoint {

        @Test
        @DisplayName("Returnerer 200 med valid=true når validatoren godkjenner nummeret")
        void returnsValidResponseWhenNumberIsValid() throws Exception {
            when(fodselsnummerValidator.validate(any()))
                    .thenReturn(ValidationResult.valid(IdType.FODSELSNUMMER));

            mockMvc.perform(post(FODSELSNUMMER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"number\":\"01010112377\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.idType").value("FODSELSNUMMER"))
                    .andExpect(jsonPath("$.number").value("01010112377"));
        }

        @Test
        @DisplayName("Returnerer 200 med valid=false når validatoren avviser nummeret")
        void returnsInvalidResponseWhenNumberIsInvalid() throws Exception {
            when(fodselsnummerValidator.validate(any()))
                    .thenReturn(ValidationResult.invalid("Ugyldig kontrollsiffer"));

            mockMvc.perform(post(FODSELSNUMMER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"number\":\"12345678901\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").value("Ugyldig kontrollsiffer"));
        }

        @Test
        @DisplayName("Returnerer 400 når request body mangler 'number'-feltet")
        void returns400WhenNumberFieldIsBlank() throws Exception {
            mockMvc.perform(post(FODSELSNUMMER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"number\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returnerer 400 ved ugyldig JSON")
        void returns400WhenBodyIsMalformed() throws Exception {
            mockMvc.perform(post(FODSELSNUMMER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("not-json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/validate/organisasjonsnummer")
    class OrganizasjonsnummerEndpoint {

        @Test
        @DisplayName("Returnerer 200 med valid=true når validatoren godkjenner nummeret")
        void returnsValidResponseWhenNumberIsValid() throws Exception {
            when(organizasjonsnummerValidator.validate(any()))
                    .thenReturn(ValidationResult.valid(IdType.ORGANISASJONSNUMMER));

            mockMvc.perform(post(ORG_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"number\":\"974760843\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.idType").value("ORGANISASJONSNUMMER"))
                    .andExpect(jsonPath("$.number").value("974760843"));
        }

        @Test
        @DisplayName("Returnerer 200 med valid=false når validatoren avviser nummeret")
        void returnsInvalidResponseWhenNumberIsInvalid() throws Exception {
            when(organizasjonsnummerValidator.validate(any()))
                    .thenReturn(ValidationResult.invalid("Ugyldig kontrollsiffer"));

            mockMvc.perform(post(ORG_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"number\":\"974760844\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").value("Ugyldig kontrollsiffer"));
        }

        @Test
        @DisplayName("Returnerer 400 når number-feltet er tomt")
        void returns400WhenNumberIsBlank() throws Exception {
            mockMvc.perform(post(ORG_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"number\":\"  \"}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
