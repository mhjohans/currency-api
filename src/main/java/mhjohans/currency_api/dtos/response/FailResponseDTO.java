package mhjohans.currency_api.dtos.response;

public class FailResponseDTO extends ResponseDTO<String> {

    public FailResponseDTO(String data) {
        super(Status.FAIL, data);
    }

}
