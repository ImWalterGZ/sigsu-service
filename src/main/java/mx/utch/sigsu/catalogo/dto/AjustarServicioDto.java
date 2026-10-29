package mx.utch.sigsu.catalogo.dto;

import jakarta.validation.constraints.*;
public record AjustarServicioDto(@Min(1) int plazoDiasHabiles, boolean activo) {}