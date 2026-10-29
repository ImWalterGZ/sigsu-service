package mx.utch.sigsu.solicitudes.dto;

import java.util.*;
import jakarta.validation.constraints.*;
public record RegistrarSolicitudDto(@NotNull UUID servicioId, @NotNull Map<String,Object> datos) {}