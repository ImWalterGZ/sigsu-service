package mx.utch.sigsu.seguridad.repository;

import mx.utch.sigsu.seguridad.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ResponsableAreaRepositorio extends JpaRepository<ResponsableArea, UUID> {
    List<ResponsableArea> findByUsuarioId(UUID usuarioId);
}
