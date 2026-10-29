package mx.utch.sigsu.solicitudes.dto;

import jakarta.validation.constraints.*;

import java.util.*;

public record RegistrarSolicitudDto(@NotNull UUID servicioId, @NotNull Map<String, Object> datos) {}
