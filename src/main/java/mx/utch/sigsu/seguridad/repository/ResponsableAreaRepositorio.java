package mx.utch.sigsu.seguridad.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import mx.utch.sigsu.seguridad.model.*;
public interface ResponsableAreaRepositorio extends JpaRepository<ResponsableArea,UUID> { List<ResponsableArea> findByUsuarioId(UUID usuarioId); }