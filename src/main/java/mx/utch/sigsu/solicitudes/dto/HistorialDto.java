package mx.utch.sigsu.solicitudes.dto;

import java.util.UUID;
import java.time.Instant;
public record HistorialDto(UUID autorId, String anterior, String nuevo, String observacion, Instant fecha) {}