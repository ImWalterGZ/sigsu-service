package mx.utch.sigsu.solicitudes.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import mx.utch.sigsu.solicitudes.model.*;
public interface HistorialRepositorio extends JpaRepository<Historial,UUID> { List<Historial> findBySolicitudIdOrderByFechaAsc(UUID solicitudId); }