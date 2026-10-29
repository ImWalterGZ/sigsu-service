package mx.utch.sigsu.solicitudes.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="adjunto") 
public class Adjunto {
 @Id public UUID id;
  public UUID solicitudId;
  public String nombre;
  public String ruta;
  public String tipo;
  public long bytes;
  public Instant creadoEn;
 public Adjunto() {}

}
