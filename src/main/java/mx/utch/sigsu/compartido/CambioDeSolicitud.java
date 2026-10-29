package mx.utch.sigsu.compartido;

import java.time.Instant;

public record CambioDeSolicitud(
        String folio,
        String estadoAnterior,
        String estadoNuevo,
        String correoEstudiante,
        String correoArea,
        Instant fecha) {}
