package mx.utch.sigsu.administracion.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="parametro") 
public class Parametro {
 @Id public String clave;
  public String valor;
 public Parametro() {}

}
