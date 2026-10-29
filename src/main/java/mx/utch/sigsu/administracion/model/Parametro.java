package mx.utch.sigsu.administracion.model;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "parametro")
public class Parametro {
    @Id public String clave;
    public String valor;

    public Parametro() {}
}
