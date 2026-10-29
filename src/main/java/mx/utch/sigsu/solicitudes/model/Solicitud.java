package mx.utch.sigsu.solicitudes.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="solicitud") 
public class Solicitud {
 @Id public UUID id;
  public String folio;
  public UUID estudianteId;
  public UUID servicioId;
  public int formularioVersion;
 @JdbcTypeCode(SqlTypes.JSON) public Map<String,Object> datos;
 @Enumerated(EnumType.STRING) public Estado estado;
  public UUID responsableId;
  public Instant venceEn;
  public Instant creadaEn;
  public Instant cerradaEn;
 @Version public int version;
 public Solicitud() {}

 public void tomar(UUID responsable) {
  if (estado!=Estado.REGISTRADA && estado!=Estado.SIN_RESPUESTA) throw conflicto("otro responsable la tomó primero o el estado no permite tomar");
  responsableId=responsable; estado=estado==Estado.REGISTRADA ? Estado.EN_ATENCION : Estado.DEMORADO;
 }
 public void cambiarEstado(Estado nuevo, String observacion) {
  if (estado!=Estado.EN_ATENCION && estado!=Estado.DEMORADO) throw conflicto("la solicitud no está en atención");
  if (nuevo!=Estado.ATENDIDA && nuevo!=Estado.RECHAZADA) throw conflicto("transición inválida");
  if (nuevo==Estado.RECHAZADA && (observacion==null || observacion.isBlank()))
    throw new mx.utch.sigsu.compartido.BusinessException(422,"rechazar exige observación");
  estado=nuevo; cerradaEn=Instant.now();
 }
 public void cancelar() {
  if (estado!=Estado.REGISTRADA) throw conflicto("solo se cancela una solicitud REGISTRADA");
  estado=Estado.CANCELADA; cerradaEn=Instant.now();
 }
 public boolean vencer(Instant ahora) {
  if (!venceEn.isBefore(ahora)) return false;
  if (estado==Estado.REGISTRADA) { estado=Estado.SIN_RESPUESTA; return true; }
  if (estado==Estado.EN_ATENCION) { estado=Estado.DEMORADO; return true; }
  return false;
 }
 private mx.utch.sigsu.compartido.BusinessException conflicto(String mensaje) { return new mx.utch.sigsu.compartido.BusinessException(409,mensaje); }

}
