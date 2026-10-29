package mx.utch.sigsu.catalogo.model;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "servicio")
public class Servicio {
    @Id public UUID id;
    public String nombre;

    @Column(columnDefinition = "text")
    public String descripcion;

    public UUID areaId;
    public UUID categoriaId;
    public int plazoDiasHabiles;
    public int versionVigente;
    public boolean activo;
    @Transient public FormularioServicio formulario;
    @Transient public Set<LocalDate> diasInhabiles = Set.of();

    public Servicio() {}

    public FormularioServicio formularioVigente() {
        return formulario;
    }

    public Instant calcularVencimiento(Instant desde) {
        ZonedDateTime fecha = desde.atZone(ZoneId.of("America/Chihuahua"));
        int restantes = plazoDiasHabiles;
        while (restantes > 0) {
            fecha = fecha.plusDays(1);
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY
                    && fecha.getDayOfWeek() != DayOfWeek.SUNDAY
                    && !diasInhabiles.contains(fecha.toLocalDate())) restantes--;
        }
        return fecha.toInstant();
    }
}
