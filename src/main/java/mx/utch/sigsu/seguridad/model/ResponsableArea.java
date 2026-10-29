package mx.utch.sigsu.seguridad.model;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "responsable_area")
public class ResponsableArea {
    @Id public UUID id;
    public UUID usuarioId;
    public UUID areaId;
    public Instant asignadaEn;

    public ResponsableArea() {}
}
