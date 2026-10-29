package mx.utch.sigsu.solicitudes.repository;

import mx.utch.sigsu.solicitudes.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface HistorialRepositorio extends JpaRepository<Historial, UUID> {
    List<Historial> findBySolicitudIdOrderByFechaAsc(UUID solicitudId);
}
