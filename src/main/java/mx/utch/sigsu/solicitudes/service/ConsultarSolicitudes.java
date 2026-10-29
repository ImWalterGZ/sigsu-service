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
@Service
public class ConsultarSolicitudes {
 private final SolicitudRepositorio solicitudes;
 private final HistorialRepositorio historial;
 private final AdjuntoRepositorio adjuntos;
 private final ICatalogo catalogo;
 private final IUsuarios usuarios;
 public ConsultarSolicitudes(SolicitudRepositorio solicitudes, HistorialRepositorio historial, AdjuntoRepositorio adjuntos, ICatalogo catalogo, IUsuarios usuarios) { this.solicitudes=solicitudes; this.historial=historial; this.adjuntos=adjuntos; this.catalogo=catalogo; this.usuarios=usuarios; }

 public Solicitud buscar(String folio) { return solicitudes.findByFolio(folio).orElseThrow(()->new BusinessException(404,"solicitud inexistente")); }
 public void exigirArea(UsuarioActual usuario,Solicitud solicitud) { if(!usuarios.areas(usuario.id()).contains(catalogo.obtener(solicitud.servicioId).areaId())) throw new BusinessException(403,"no perteneces al área del servicio"); }
 public void exigirDueno(UsuarioActual usuario,Solicitud solicitud) { if(!solicitud.estudianteId.equals(usuario.id())) throw new BusinessException(403,"la solicitud pertenece a otro estudiante"); }
 public void exigirLectura(UsuarioActual usuario,Solicitud solicitud) {
  if(usuario.tieneRol("ADMINISTRADOR") || solicitud.estudianteId.equals(usuario.id())) return;
  if(usuario.tieneRol("RESPONSABLE")) { exigirArea(usuario,solicitud); return; }
  throw new BusinessException(403,"sin permiso para consultar esta solicitud");
 }
 public DetalleSolicitudDto detalle(String folio,UsuarioActual usuario) {
  Solicitud solicitud=buscar(folio); exigirLectura(usuario,solicitud);
  return new DetalleSolicitudDto(SolicitudDto.de(solicitud),historial.findBySolicitudIdOrderByFechaAsc(solicitud.id).stream().map(h->new HistorialDto(h.autorId,h.anterior==null?null:h.anterior.name(),h.nuevo.name(),h.observacion,h.fecha)).toList(),adjuntos.findBySolicitudId(solicitud.id).stream().map(a->new AdjuntoDto(a.id,a.nombre,a.tipo,a.bytes)).toList());
 }
 public List<SolicitudDto> propias(UsuarioActual usuario) {
  if(!usuario.tieneRol("ESTUDIANTE")) throw new BusinessException(403,"se requiere rol ESTUDIANTE");
  return solicitudes.findByEstudianteIdOrderByCreadaEnDesc(usuario.id()).stream().map(SolicitudDto::de).toList();
 }
 public List<SolicitudDto> bandeja(UsuarioActual usuario,Estado estado) {
  var areas=usuarios.areas(usuario.id());
  if(estado!=null && !Estado.ABIERTOS.contains(estado)) return List.of();
  return solicitudes.findByEstadoInOrderByVenceEnAsc(estado==null?Estado.ABIERTOS:Set.of(estado)).stream()
    .filter(s->areas.contains(catalogo.obtener(s.servicioId).areaId())).map(SolicitudDto::de).toList();
 }

}