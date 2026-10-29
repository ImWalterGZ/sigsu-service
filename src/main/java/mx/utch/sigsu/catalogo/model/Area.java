package mx.utch.sigsu.catalogo.model;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "area")
public class Area {
    @Id public UUID id;
    public String nombre;
    public String correo;

    public Area() {}
}
