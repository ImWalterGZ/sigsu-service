package mx.utch.sigsu.solicitudes.repository;

import mx.utch.sigsu.solicitudes.model.*;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.*;

public interface SolicitudRepositorio extends JpaRepository<Solicitud, UUID> {
    Optional<Solicitud> findByFolio(String folio);

    List<Solicitud> findByEstudianteIdOrderByCreadaEnDesc(UUID estudianteId);

    Optional<Solicitud> findFirstByEstudianteIdAndServicioIdAndEstadoIn(
            UUID estudianteId, UUID servicioId, Collection<Estado> estados);

    List<Solicitud> findByEstadoInOrderByVenceEnAsc(Collection<Estado> estados);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value =
                    "UPDATE solicitud SET responsable_id=:responsable, estado=:nuevo,"
                        + " version=version+1 WHERE id=:id AND version=:version AND estado IN"
                        + " ('REGISTRADA','SIN_RESPUESTA')",
            nativeQuery = true)
    int tomar(
            @Param("id") UUID id,
            @Param("version") int version,
            @Param("responsable") UUID responsable,
            @Param("nuevo") String nuevo);

    @Query(
            value =
                    "SELECT * FROM solicitud WHERE estado IN ('REGISTRADA','EN_ATENCION') AND"
                        + " vence_en < :ahora ORDER BY vence_en FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<Solicitud> vencidasBloqueadas(@Param("ahora") Instant ahora);
}
