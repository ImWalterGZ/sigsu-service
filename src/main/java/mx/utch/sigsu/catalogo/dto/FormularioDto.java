package mx.utch.sigsu.catalogo.dto;

import java.util.Map;
import jakarta.validation.constraints.NotNull;
public record FormularioDto(@NotNull Map<String,Object> esquema) {}