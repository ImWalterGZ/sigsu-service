package mx.utch.sigsu.compartido.dto;

import java.util.List;

public record ErrorResponseDto(
        int status, String mensaje, String folioExistente, List<ErrorCampo> errores) {}
