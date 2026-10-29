package mx.utch.sigsu.seguridad.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="usuario") 
public class Usuario {
 @Id public UUID id;
  public String correo;
  public String nombre;
  public String contrasena;
  public boolean activo;
 @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="usuario_rol", joinColumns=@JoinColumn(name="usuario_id")) @Enumerated(EnumType.STRING) @Column(name="rol") public Set<Rol> roles = new HashSet<>();
 public Usuario() {}

}
