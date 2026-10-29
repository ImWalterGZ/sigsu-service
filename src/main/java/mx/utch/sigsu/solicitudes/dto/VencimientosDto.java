package mx.utch.sigsu.solicitudes.dto;

import java.util.List;
public record VencimientosDto(int sinRespuesta, int demoradas, List<String> foliosSinRespuesta, List<String> foliosDemoradas) {}