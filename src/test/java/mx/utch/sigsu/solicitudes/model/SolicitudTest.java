package mx.utch.sigsu.solicitudes.model;

import static org.junit.jupiter.api.Assertions.*;

import mx.utch.sigsu.compartido.BusinessException;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class SolicitudTest {
    @Test
    void cancelarSoloRegistrada() {
        for (Estado estado : Estado.values()) {
            Solicitud solicitud = new Solicitud();
            solicitud.estado = estado;
            if (estado == Estado.REGISTRADA) {
                solicitud.cancelar();
                assertEquals(Estado.CANCELADA, solicitud.estado);
                assertNotNull(solicitud.cerradaEn);
            } else
                assertEquals(
                        409,
                        assertThrows(BusinessException.class, solicitud::cancelar)
                                .respuesta()
                                .status());
        }
    }

    @Test
    void rechazarExigeObservacion() {
        Solicitud solicitud = new Solicitud();
        solicitud.estado = Estado.EN_ATENCION;
        assertEquals(
                422,
                assertThrows(
                                BusinessException.class,
                                () -> solicitud.cambiarEstado(Estado.RECHAZADA, " "))
                        .respuesta()
                        .status());
        solicitud.cambiarEstado(Estado.RECHAZADA, "No procede");
        assertNotNull(solicitud.cerradaEn);
    }

    @Test
    void tomarSinRespuestaPasaADemorado() {
        Solicitud solicitud = new Solicitud();
        solicitud.estado = Estado.SIN_RESPUESTA;
        UUID responsable = UUID.randomUUID();
        solicitud.tomar(responsable);
        assertEquals(Estado.DEMORADO, solicitud.estado);
        assertEquals(responsable, solicitud.responsableId);
    }

    @Test
    void vencimientoSoloAfectaAbiertasConPlazoVencido() {
        Solicitud solicitud = new Solicitud();
        solicitud.estado = Estado.EN_ATENCION;
        solicitud.venceEn = Instant.parse("2026-10-30T10:00:00Z");
        assertFalse(solicitud.vencer(solicitud.venceEn));
        assertTrue(solicitud.vencer(solicitud.venceEn.plusSeconds(1)));
        assertEquals(Estado.DEMORADO, solicitud.estado);
        assertFalse(solicitud.vencer(solicitud.venceEn.plusSeconds(2)));
    }
}
