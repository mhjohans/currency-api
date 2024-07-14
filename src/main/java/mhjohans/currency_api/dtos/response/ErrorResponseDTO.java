package mhjohans.currency_api.dtos.response;

import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;

public class ErrorResponseDTO extends ResponseDTO<Map<String, Object>> {

    private final String message;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private final Optional<Integer> code;

    public ErrorResponseDTO(String message) {
        super(Status.ERROR, null);
        this.message = message;
        this.code = Optional.empty();
    }

    public ErrorResponseDTO(String message, int code, Map<String, Object> data) {
        super(Status.ERROR, data);
        this.message = message;
        this.code = Optional.of(code);
    }

    public String getMessage() {
        return message;
    }

    public Optional<Integer> getCode() {
        return code;
    }

}
