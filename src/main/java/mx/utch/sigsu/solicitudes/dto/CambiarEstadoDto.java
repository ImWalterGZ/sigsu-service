package mx.utch.sigsu.solicitudes.dto;

import jakarta.validation.constraints.NotNull;
import mx.utch.sigsu.solicitudes.model.Estado;
public record CambiarEstadoDto(@NotNull Estado estado, String observacion) {}