package mx.utch.sigsu.solicitudes.service;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.utch.sigsu.solicitudes.model.*;
import mx.utch.sigsu.solicitudes.repository.*;
import mx.utch.sigsu.solicitudes.dto.*;
import mx.utch.sigsu.compartido.*;
import mx.utch.sigsu.catalogo.service.ICatalogo;
import mx.utch.sigsu.seguridad.service.IUsuarios;
import org.springframework.context.ApplicationEventPublisher;
@Service
public class RegistrarCambio {
 private final HistorialRepositorio historial;
 private final ICatalogo catalogo;
 private final IUsuarios usuarios;
 private final ApplicationEventPublisher eventos;
 public RegistrarCambio(HistorialRepositorio historial, ICatalogo catalogo, IUsuarios usuarios, ApplicationEventPublisher eventos) { this.historial=historial; this.catalogo=catalogo; this.usuarios=usuarios; this.eventos=eventos; }

 public void registrar(Solicitud solicitud,UUID autor,Estado anterior,String observacion,Instant fecha) {
  Historial cambio=new Historial(); cambio.id=UUID.randomUUID(); cambio.solicitudId=solicitud.id; cambio.autorId=autor;
  cambio.anterior=anterior; cambio.nuevo=solicitud.estado; cambio.observacion=observacion; cambio.fecha=fecha; historial.save(cambio);
  eventos.publishEvent(new CambioDeSolicitud(solicitud.folio,anterior==null?null:anterior.name(),solicitud.estado.name(),usuarios.obtener(solicitud.estudianteId).correo(),catalogo.obtener(solicitud.servicioId).correoArea(),fecha));
 }

}