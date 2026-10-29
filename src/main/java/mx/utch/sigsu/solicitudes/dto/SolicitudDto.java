package mx.utch.sigsu.solicitudes.dto;

import java.util.*;
import java.time.Instant;
import mx.utch.sigsu.solicitudes.model.Solicitud;
public record SolicitudDto(UUID id, String folio, UUID estudianteId, UUID servicioId, int formularioVersion, Map<String,Object> datos, String estado, UUID responsableId, Instant venceEn, Instant creadaEn, Instant cerradaEn, int version) {
 public static SolicitudDto de(Solicitud solicitud) { return new SolicitudDto(solicitud.id,solicitud.folio,solicitud.estudianteId,solicitud.servicioId,solicitud.formularioVersion,solicitud.datos,solicitud.estado.name(),solicitud.responsableId,solicitud.venceEn,solicitud.creadaEn,solicitud.cerradaEn,solicitud.version); }
}