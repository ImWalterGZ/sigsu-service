package mx.utch.sigsu.solicitudes.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import mx.utch.sigsu.solicitudes.model.*;
public interface AdjuntoRepositorio extends JpaRepository<Adjunto,UUID> { List<Adjunto> findBySolicitudId(UUID solicitudId); }