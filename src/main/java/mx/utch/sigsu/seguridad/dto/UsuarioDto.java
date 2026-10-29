package mx.utch.sigsu.seguridad.dto;

import java.util.*;
public record UsuarioDto(UUID id, String correo, String nombre, Set<String> roles, boolean activo) {}