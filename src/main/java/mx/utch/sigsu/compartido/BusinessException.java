package mx.utch.sigsu.compartido;

import mx.utch.sigsu.compartido.dto.*;

import java.util.List;

public class BusinessException extends RuntimeException {
    private final ErrorResponseDto respuesta;

    public BusinessException(int status, String mensaje) {
        this(status, mensaje, null, List.of());
    }

    public BusinessException(int status, String mensaje, String folio, List<ErrorCampo> errores) {
        super(mensaje);
        respuesta = new ErrorResponseDto(status, mensaje, folio, errores);
    }

    public ErrorResponseDto respuesta() {
        return respuesta;
    }
}
