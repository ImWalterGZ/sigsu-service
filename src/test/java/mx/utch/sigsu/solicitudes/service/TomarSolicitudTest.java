package mx.utch.sigsu.solicitudes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import mx.utch.sigsu.compartido.*;
import mx.utch.sigsu.solicitudes.model.*;
import mx.utch.sigsu.solicitudes.repository.SolicitudRepositorio;

import org.junit.jupiter.api.Test;

import java.util.*;

class TomarSolicitudTest {
    @Test
    void ceroFilasEsConflictoSinHistorial() {
        var repositorio = mock(SolicitudRepositorio.class);
        var consultas = mock(ConsultarSolicitudes.class);
        var cambios = mock(RegistrarCambio.class);
        Solicitud solicitud = new Solicitud();
        solicitud.id = UUID.randomUUID();
        solicitud.estado = Estado.REGISTRADA;
        when(consultas.buscar("folio")).thenReturn(solicitud);
        var usuario =
                new UsuarioActual(
                        UUID.randomUUID(),
                        "resp@utch.edu.mx",
                        "Responsable",
                        Set.of("RESPONSABLE"));
        var servicio = new TomarSolicitud(repositorio, consultas, cambios);
        assertEquals(
                409,
                assertThrows(BusinessException.class, () -> servicio.tomar("folio", usuario, 0))
                        .respuesta()
                        .status());
        verify(repositorio).tomar(solicitud.id, 0, usuario.id(), "EN_ATENCION");
        verifyNoInteractions(cambios);
    }
}
