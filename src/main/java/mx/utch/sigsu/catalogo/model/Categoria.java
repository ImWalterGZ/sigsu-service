package mx.utch.sigsu.catalogo.model;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id public UUID id;
    public String nombre;

    public Categoria() {}
}
