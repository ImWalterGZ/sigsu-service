package mx.utch.sigsu.solicitudes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import mx.utch.sigsu.catalogo.dto.CondicionesDto;
import mx.utch.sigsu.catalogo.service.ICatalogo;
import mx.utch.sigsu.compartido.*;
import mx.utch.sigsu.solicitudes.dto.*;
import mx.utch.sigsu.solicitudes.model.*;
import mx.utch.sigsu.solicitudes.repository.SolicitudRepositorio;

import org.junit.jupiter.api.Test;
import org.postgresql.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.*;

import java.time.Instant;
import java.util.*;

class RegistrarSolicitudTest {
    @Test
    void duplicadoConsultaFolioDespuesDelRollback() {
        var solicitudes = mock(SolicitudRepositorio.class);
        var catalogo = mock(ICatalogo.class);
        var folios = mock(Folio.class);
        var cambios = mock(RegistrarCambio.class);
        var gestor = mock(PlatformTransactionManager.class);
        var estado = mock(TransactionStatus.class);
        when(gestor.getTransaction(any())).thenReturn(estado);
        UUID servicio = UUID.randomUUID(), estudiante = UUID.randomUUID();
        var datos = new RegistrarSolicitudDto(servicio, Map.of());
        when(catalogo.condiciones(eq(servicio), any()))
                .thenReturn(
                        new CondicionesDto(
                                servicio,
                                1,
                                UUID.randomUUID(),
                                "area@utch.edu.mx",
                                Map.of(),
                                Instant.now()));
        when(catalogo.validar(servicio, 1, Map.of())).thenReturn(List.of());
        var error =
                new PSQLException(
                        new ServerErrorMessage(
                                "SERROR\0C23505\0Mduplicado\0nsolicitud_abierta\0\0"));
        when(solicitudes.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicado", error));
        Solicitud existente = new Solicitud();
        existente.folio = "SIGSU-2026-000001";
        when(solicitudes.findFirstByEstudianteIdAndServicioIdAndEstadoIn(
                        estudiante, servicio, Estado.ABIERTOS))
                .thenReturn(Optional.of(existente));
        RegistrarSolicitud registro =
                new RegistrarSolicitud(
                        solicitudes, catalogo, folios, cambios, new TransactionTemplate(gestor));
        BusinessException conflicto =
                assertThrows(
                        BusinessException.class,
                        () ->
                                registro.registrar(
                                        new UsuarioActual(
                                                estudiante,
                                                "a@b.mx",
                                                "Estudiante",
                                                Set.of("ESTUDIANTE")),
                                        datos));
        assertEquals(409, conflicto.respuesta().status());
        assertEquals(existente.folio, conflicto.respuesta().folioExistente());
        var orden = inOrder(gestor, solicitudes);
        orden.verify(gestor).rollback(estado);
        orden.verify(solicitudes)
                .findFirstByEstudianteIdAndServicioIdAndEstadoIn(
                        estudiante, servicio, Estado.ABIERTOS);
        verifyNoInteractions(cambios);
    }
}
