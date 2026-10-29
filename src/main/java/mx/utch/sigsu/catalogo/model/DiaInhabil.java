package mx.utch.sigsu.catalogo.model;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "dia_inhabil")
public class DiaInhabil {
    @Id public LocalDate fecha;
    public String motivo;

    public DiaInhabil() {}
}
