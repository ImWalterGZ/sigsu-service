package mx.utch.sigsu.solicitudes.repository;

import mx.utch.sigsu.solicitudes.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AdjuntoRepositorio extends JpaRepository<Adjunto, UUID> {
    List<Adjunto> findBySolicitudId(UUID solicitudId);
}
