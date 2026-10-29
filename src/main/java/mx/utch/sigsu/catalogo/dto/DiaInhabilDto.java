package mx.utch.sigsu.catalogo.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
public record DiaInhabilDto(@NotNull LocalDate fecha, @NotBlank String motivo) {}