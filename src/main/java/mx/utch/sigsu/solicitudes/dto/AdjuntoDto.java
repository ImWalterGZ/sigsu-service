package mx.utch.sigsu.solicitudes.dto;

import java.util.UUID;

public record AdjuntoDto(UUID id, String nombre, String tipo, long bytes) {}
