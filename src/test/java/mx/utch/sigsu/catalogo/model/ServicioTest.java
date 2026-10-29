package mx.utch.sigsu.catalogo.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Set;

class ServicioTest {
    @Test
    void saltaFinDeSemanaYDiaInhabilConservandoHoraLocal() {
        Servicio servicio = new Servicio();
        servicio.plazoDiasHabiles = 3;
        servicio.diasInhabiles = Set.of(LocalDate.parse("2026-11-02"));
        var desde = ZonedDateTime.of(2026, 10, 30, 10, 15, 0, 0, ZoneId.of("America/Chihuahua"));
        assertEquals(
                ZonedDateTime.of(2026, 11, 5, 10, 15, 0, 0, ZoneId.of("America/Chihuahua"))
                        .toInstant(),
                servicio.calcularVencimiento(desde.toInstant()));
    }

    @Test
    void plazoDeUnDiaDesdeViernesVenceElLunes() {
        Servicio servicio = new Servicio();
        servicio.plazoDiasHabiles = 1;
        assertEquals(
                Instant.parse("2026-10-26T16:00:00Z"),
                servicio.calcularVencimiento(Instant.parse("2026-10-23T16:00:00Z")));
    }
}
