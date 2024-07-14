package mhjohans.currency_api.dtos.response;

public class SuccessResponseDTO<T> extends ResponseDTO<T> {

    public SuccessResponseDTO(T data) {
        super(Status.SUCCESS, data);
    }

}
