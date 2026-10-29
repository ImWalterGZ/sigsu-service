package mx.utch.sigsu.catalogo.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record DiaInhabilDto(@NotNull LocalDate fecha, @NotBlank String motivo) {}
