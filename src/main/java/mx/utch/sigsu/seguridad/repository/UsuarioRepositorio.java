package mx.utch.sigsu.seguridad.repository;

import mx.utch.sigsu.seguridad.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UsuarioRepositorio extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);
}
