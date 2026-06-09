package no.example.nationalidvalidator.model;

/** Represents the type of Norwegian identifier that was validated. */
public enum IdType {

    /** Standard Norwegian national identity number (fødselsnummer). */
    FODSELSNUMMER,

    /** D-number assigned to foreign nationals residing temporarily in Norway. */
    D_NUMBER,

    /** H-number assigned to patients without a valid fødselsnummer. */
    H_NUMBER,

    /** Norwegian organization number (organisasjonsnummer). */
    ORGANISASJONSNUMMER
}
