package mx.utch.sigsu.seguridad.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import mx.utch.sigsu.seguridad.model.*;
public interface UsuarioRepositorio extends JpaRepository<Usuario,UUID> { Optional<Usuario> findByCorreo(String correo); }