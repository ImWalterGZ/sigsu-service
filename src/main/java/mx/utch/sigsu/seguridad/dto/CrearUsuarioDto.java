package mx.utch.sigsu.seguridad.dto;

import java.util.*;
import jakarta.validation.constraints.*;
public record CrearUsuarioDto(@NotBlank @Email String correo, @NotBlank String nombre, @NotBlank @Size(min=8) String contrasena, @NotEmpty Set<@Pattern(regexp="ESTUDIANTE|RESPONSABLE|ADMINISTRADOR") String> roles) {}