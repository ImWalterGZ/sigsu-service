package mx.utch.sigsu.catalogo.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="categoria") 
public class Categoria {
 @Id public UUID id;
  public String nombre;
 public Categoria() {}

}
