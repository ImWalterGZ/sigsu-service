package mx.utch.sigsu.solicitudes.dto;

import java.time.Instant;
import java.util.UUID;

public record HistorialDto(
        UUID autorId, String anterior, String nuevo, String observacion, Instant fecha) {}
