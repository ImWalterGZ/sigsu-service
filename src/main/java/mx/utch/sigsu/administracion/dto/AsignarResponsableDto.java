package mx.utch.sigsu.administracion.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
public record AsignarResponsableDto(@NotNull UUID areaId) {}