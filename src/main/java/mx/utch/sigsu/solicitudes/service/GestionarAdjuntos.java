package mx.utch.sigsu.solicitudes.service;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.utch.sigsu.solicitudes.model.*;
import mx.utch.sigsu.solicitudes.repository.*;
import mx.utch.sigsu.solicitudes.dto.*;
import mx.utch.sigsu.compartido.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.*;
import org.springframework.http.*;
@Service
public class GestionarAdjuntos {
 private final ConsultarSolicitudes consultas;
 private final AdjuntoRepositorio adjuntos;
 private final AlmacenDeAdjuntos almacen;
 private final TransactionTemplate transacciones;
 public GestionarAdjuntos(ConsultarSolicitudes consultas, AdjuntoRepositorio adjuntos, AlmacenDeAdjuntos almacen, TransactionTemplate transacciones) { this.consultas=consultas; this.adjuntos=adjuntos; this.almacen=almacen; this.transacciones=transacciones; }

 public AdjuntoDto guardar(String folio,UsuarioActual estudiante,MultipartFile archivo) {
  Solicitud solicitud=consultas.buscar(folio); consultas.exigirDueno(estudiante,solicitud);
  UUID id=UUID.randomUUID(); String ruta=almacen.guardar(id,archivo);
  try { return transacciones.execute(transaccion -> {
   Adjunto adjunto=new Adjunto(); adjunto.id=id; adjunto.solicitudId=solicitud.id;
   String nombre=archivo.getOriginalFilename(); adjunto.nombre=nombre==null?"archivo":java.nio.file.Path.of(nombre).getFileName().toString();
   adjunto.ruta=ruta; adjunto.tipo=archivo.getContentType()==null?"application/octet-stream":archivo.getContentType(); adjunto.bytes=archivo.getSize(); adjunto.creadoEn=Instant.now(); adjuntos.saveAndFlush(adjunto);
   return new AdjuntoDto(id,adjunto.nombre,adjunto.tipo,adjunto.bytes);
  }); } catch(RuntimeException error) { almacen.borrar(ruta); throw error; }
 }
 public ResponseEntity<Resource> descargar(String folio,UUID id,UsuarioActual usuario) {
  Solicitud solicitud=consultas.buscar(folio); consultas.exigirLectura(usuario,solicitud);
  Adjunto adjunto=adjuntos.findById(id).filter(a->a.solicitudId.equals(solicitud.id)).orElseThrow(()->new BusinessException(404,"adjunto inexistente"));
  Resource recurso=new FileSystemResource(almacen.obtener(adjunto.ruta)); if(!recurso.exists()) throw new BusinessException(404,"archivo inexistente");
  return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(adjunto.nombre,java.nio.charset.StandardCharsets.UTF_8).build().toString()).body(recurso);
 }

}