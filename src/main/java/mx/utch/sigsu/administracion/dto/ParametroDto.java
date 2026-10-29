package mx.utch.sigsu.administracion.dto;

import jakarta.validation.constraints.NotBlank;
public record ParametroDto(@NotBlank String valor) {}