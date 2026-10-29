package mx.utch.sigsu.solicitudes.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="historial") 
public class Historial {
 @Id public UUID id;
  public UUID solicitudId;
  public UUID autorId;
 @Enumerated(EnumType.STRING) public Estado anterior;
 @Enumerated(EnumType.STRING) public Estado nuevo;
 @Column(columnDefinition="text") public String observacion;
  public Instant fecha;
 public Historial() {}

}
