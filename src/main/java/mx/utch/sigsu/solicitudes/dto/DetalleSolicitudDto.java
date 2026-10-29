package mx.utch.sigsu.solicitudes.dto;

import java.util.*;

public record DetalleSolicitudDto(
        SolicitudDto solicitud, List<HistorialDto> historial, List<AdjuntoDto> adjuntos) {}
