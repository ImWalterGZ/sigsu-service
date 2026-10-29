package mx.utch.sigsu.solicitudes.service;

import mx.utch.sigsu.catalogo.service.ICatalogo;
import mx.utch.sigsu.compartido.*;
import mx.utch.sigsu.solicitudes.dto.*;
import mx.utch.sigsu.solicitudes.model.*;
import mx.utch.sigsu.solicitudes.repository.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.*;
import java.util.*;

@Service
public class RegistrarSolicitud {
    private final SolicitudRepositorio solicitudes;
    private final ICatalogo catalogo;
    private final Folio folios;
    private final RegistrarCambio cambios;
    private final TransactionTemplate transacciones;

    public RegistrarSolicitud(
            SolicitudRepositorio solicitudes,
            ICatalogo catalogo,
            Folio folios,
            RegistrarCambio cambios,
            TransactionTemplate transacciones) {
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
        this.folios = folios;
        this.cambios = cambios;
        this.transacciones = transacciones;
    }

    public SolicitudDto registrar(UsuarioActual estudiante, RegistrarSolicitudDto datos) {
        Instant ahora = Instant.now();
        try {
            return transacciones.execute(
                    transaccion -> {
                        var condiciones = catalogo.condiciones(datos.servicioId(), ahora);
                        var errores =
                                catalogo.validar(
                                        datos.servicioId(), condiciones.version(), datos.datos());
                        if (!errores.isEmpty())
                            throw new BusinessException(422, "formulario inválido", null, errores);
                        Solicitud solicitud = new Solicitud();
                        solicitud.id = UUID.randomUUID();
                        solicitud.folio = folios.siguiente(ahora);
                        solicitud.estudianteId = estudiante.id();
                        solicitud.servicioId = datos.servicioId();
                        solicitud.formularioVersion = condiciones.version();
                        solicitud.datos = datos.datos();
                        solicitud.estado = Estado.REGISTRADA;
                        solicitud.venceEn = condiciones.venceEn();
                        solicitud.creadaEn = ahora;
                        solicitudes.saveAndFlush(solicitud);
                        cambios.registrar(solicitud, estudiante.id(), null, "registro", ahora);
                        return SolicitudDto.de(solicitud);
                    });
        } catch (DataIntegrityViolationException error) {
            if (!esDuplicado(error)) throw error;
            // TransactionTemplate ya hizo rollback; esta consulta no usa la transacción fallida.
            var existente =
                    solicitudes.findFirstByEstudianteIdAndServicioIdAndEstadoIn(
                            estudiante.id(), datos.servicioId(), Estado.ABIERTOS);
            String folio = existente.map(s -> s.folio).orElse(null);
            throw new BusinessException(
                    409,
                    folio == null
                            ? "solicitud abierta duplicada"
                            : "ya tienes " + folio + " en curso",
                    folio,
                    List.of());
        }
    }

    private boolean esDuplicado(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause())
            if (causa instanceof org.postgresql.util.PSQLException postgres
                    && "23505".equals(postgres.getSQLState())
                    && postgres.getServerErrorMessage() != null
                    && "solicitud_abierta".equals(postgres.getServerErrorMessage().getConstraint()))
                return true;
        return false;
    }
}
