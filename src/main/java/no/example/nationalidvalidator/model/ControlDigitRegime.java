package no.example.nationalidvalidator.model;

/**
 * Specifies which control digit calculation regime(s) validate a given number.
 *
 * <p>Ref. Skatteetaten: https://www.skatteetaten.no/person/folkeregister/om-fonnummeret/
 */
public enum ControlDigitRegime {

    /** Legacy mod-11 algorithm (current regime until 2032). */
    LEGACY,

    /** Norwegian 2032 PID control digit calculation (future regime from 2032). */
    PID_2032,

    /** Both legacy and 2032 regimes validate the number. */
    BOTH,

    /** No regime validates the number. */
    NONE
}
