package mx.utch.sigsu.solicitudes.service;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.utch.sigsu.solicitudes.model.*;
import mx.utch.sigsu.solicitudes.repository.*;
import mx.utch.sigsu.solicitudes.dto.*;
import mx.utch.sigsu.compartido.*;

@Service
public class CambiarEstado {
 private final SolicitudRepositorio solicitudes;
 private final ConsultarSolicitudes consultas;
 private final RegistrarCambio cambios;
 public CambiarEstado(SolicitudRepositorio solicitudes, ConsultarSolicitudes consultas, RegistrarCambio cambios) { this.solicitudes=solicitudes; this.consultas=consultas; this.cambios=cambios; }

 @Transactional public SolicitudDto cambiar(String folio,UsuarioActual responsable,CambiarEstadoDto datos) {
  Solicitud solicitud=consultas.buscar(folio); consultas.exigirArea(responsable,solicitud);
  if(!responsable.id().equals(solicitud.responsableId)) throw new BusinessException(403,"solo puede cerrar quien tomó la solicitud");
  Estado anterior=solicitud.estado; solicitud.cambiarEstado(datos.estado(),datos.observacion()); solicitudes.saveAndFlush(solicitud);
  cambios.registrar(solicitud,responsable.id(),anterior,datos.observacion(),Instant.now()); return SolicitudDto.de(solicitud);
 }
 @Transactional public SolicitudDto cancelar(String folio,UsuarioActual estudiante) {
  Solicitud solicitud=consultas.buscar(folio); consultas.exigirDueno(estudiante,solicitud); Estado anterior=solicitud.estado;
  solicitud.cancelar(); solicitudes.saveAndFlush(solicitud); cambios.registrar(solicitud,estudiante.id(),anterior,"cancelación del estudiante",Instant.now()); return SolicitudDto.de(solicitud);
 }

}