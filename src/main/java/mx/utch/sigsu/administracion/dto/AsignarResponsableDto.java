package mx.utch.sigsu.administracion.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AsignarResponsableDto(@NotNull UUID areaId) {}
