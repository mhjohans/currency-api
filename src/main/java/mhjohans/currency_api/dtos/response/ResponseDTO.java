package mhjohans.currency_api.dtos.response;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) in JSend format for responses from the API.
 */
public abstract class ResponseDTO<T> {

    public enum Status {
        @JsonProperty("success")
        SUCCESS, @JsonProperty("fail")
        FAIL, @JsonProperty("error")
        ERROR
    }

    private final Status status;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private final Optional<T> data;

    protected ResponseDTO(Status status, T data) {
        this.status = status;
        this.data = Optional.ofNullable(data);
    }

    public Optional<T> getData() {
        return data;
    }

    public Status getStatus() {
        return status;
    }

}
