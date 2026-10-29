package mx.utch.sigsu.solicitudes.repository;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import mx.utch.sigsu.compartido.BusinessException;
@Component
public class AlmacenDeAdjuntos {
 private final Path raiz;
 public AlmacenDeAdjuntos(@Value("${sigsu.adjuntos.ruta:/var/sigsu/adjuntos}") String ruta) { raiz=Path.of(ruta).toAbsolutePath().normalize(); }
 public String guardar(UUID id,MultipartFile archivo) {
  if(archivo.isEmpty() || archivo.getSize()>5*1024*1024) throw new BusinessException(422,"el adjunto debe tener entre 1 byte y 5 MB");
  try { Files.createDirectories(raiz); Files.copy(archivo.getInputStream(),raiz.resolve(id.toString())); return id.toString(); }
  catch(IOException error) { throw new IllegalStateException("no fue posible guardar el adjunto",error); }
 }
 public Path obtener(String ruta) { Path archivo=raiz.resolve(ruta).normalize(); if(!archivo.startsWith(raiz)) throw new BusinessException(404,"adjunto inexistente"); return archivo; }
 public void borrar(String ruta) { try { Files.deleteIfExists(obtener(ruta)); } catch(IOException error) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("no fue posible limpiar el archivo {}",ruta,error); } }
}