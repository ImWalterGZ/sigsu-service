package mx.utch.sigsu.compartido;

import java.util.*;
import org.springframework.security.core.Authentication;
public record UsuarioActual(UUID id, String correo, String nombre, Set<String> roles) {
 public boolean tieneRol(String rol) { return roles.contains(rol); }
 public static UsuarioActual de(Authentication autenticacion) { return ((PrincipalUsuario)autenticacion.getPrincipal()).usuario(); }
}