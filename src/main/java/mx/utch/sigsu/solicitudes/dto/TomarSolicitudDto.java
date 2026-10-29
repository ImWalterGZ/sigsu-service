package mx.utch.sigsu.solicitudes.dto;

import jakarta.validation.constraints.Min;

public record TomarSolicitudDto(@Min(0) int version) {}
