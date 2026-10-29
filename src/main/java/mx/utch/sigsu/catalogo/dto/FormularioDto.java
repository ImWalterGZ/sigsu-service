package mx.utch.sigsu.catalogo.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record FormularioDto(@NotNull Map<String, Object> esquema) {}
