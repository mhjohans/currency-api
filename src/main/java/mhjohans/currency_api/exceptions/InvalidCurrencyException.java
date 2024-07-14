package mhjohans.currency_api.exceptions;

/*
 * Exception that is thrown when an invalid currency code is provided in the request
 */
public class InvalidCurrencyException extends Exception {

    public InvalidCurrencyException(String message) {
        super(message);
    }

}
