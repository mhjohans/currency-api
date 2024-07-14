package mhjohans.currency_api.exceptions;

import org.springframework.web.client.RestClientResponseException;

/*
 * Exception thrown when the external currency rate API returns an error
 */
public class ExternalApiException extends Exception {

    private final RestClientResponseException clientResponseException;

    public ExternalApiException(String message,
            RestClientResponseException clientResponseException) {
        super(message, clientResponseException);
        this.clientResponseException = clientResponseException;
    }

    public RestClientResponseException getClientResponseException() {
        return clientResponseException;
    }

    @Override
    public String getMessage() {
        return String.format(
                "Could not get a valid response from external currency rate API:%nResponse [%s]",
                clientResponseException.getMessage());
    }

}
