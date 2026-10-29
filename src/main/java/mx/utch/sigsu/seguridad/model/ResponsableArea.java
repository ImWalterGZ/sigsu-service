package mx.utch.sigsu.seguridad.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="responsable_area") 
public class ResponsableArea {
 @Id public UUID id;
  public UUID usuarioId;
  public UUID areaId;
  public Instant asignadaEn;
 public ResponsableArea() {}

}
