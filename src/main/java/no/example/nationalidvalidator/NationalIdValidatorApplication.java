package no.example.nationalidvalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point for the National ID Validator application. */
@SpringBootApplication
public class NationalIdValidatorApplication {

    /** Private constructor — utility entry point. */
    protected NationalIdValidatorApplication() {
    }

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(NationalIdValidatorApplication.class, args);
    }
}
