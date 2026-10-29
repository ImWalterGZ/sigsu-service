package mx.utch.sigsu.catalogo.dto;

import java.time.Instant;
import java.util.*;

public record CondicionesDto(
        UUID servicioId,
        int version,
        UUID areaId,
        String correoArea,
        Map<String, Object> esquema,
        Instant venceEn) {}
