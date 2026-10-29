package mx.utch.sigsu.catalogo.dto;

import java.util.*;

public record ServicioDto(
        UUID id,
        String nombre,
        String descripcion,
        UUID areaId,
        String correoArea,
        UUID categoriaId,
        int plazoDiasHabiles,
        int versionVigente,
        boolean activo,
        Map<String, Object> esquema) {}
