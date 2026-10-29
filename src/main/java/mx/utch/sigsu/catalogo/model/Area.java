package mx.utch.sigsu.catalogo.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="area") 
public class Area {
 @Id public UUID id;
  public String nombre;
  public String correo;
 public Area() {}

}
