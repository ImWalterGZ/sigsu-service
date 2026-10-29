package mx.utch.sigsu.catalogo.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="dia_inhabil") 
public class DiaInhabil {
 @Id public LocalDate fecha;
  public String motivo;
 public DiaInhabil() {}

}
