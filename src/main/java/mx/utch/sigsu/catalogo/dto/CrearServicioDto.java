package mx.utch.sigsu.catalogo.dto;

import java.util.*;
import jakarta.validation.constraints.*;
public record CrearServicioDto(@NotBlank String nombre, @NotNull String descripcion, @NotNull UUID areaId, @NotNull UUID categoriaId, @Min(1) int plazoDiasHabiles, @NotNull Map<String,Object> esquema) {}