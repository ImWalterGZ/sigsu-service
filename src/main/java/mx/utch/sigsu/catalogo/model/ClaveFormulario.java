package mx.utch.sigsu.catalogo.model;

import java.io.Serializable;
import java.util.*;
public class ClaveFormulario implements Serializable {
 public UUID servicioId; public int version;
 public ClaveFormulario() {}
 @Override public boolean equals(Object otro) { return otro instanceof ClaveFormulario clave && Objects.equals(servicioId,clave.servicioId) && version==clave.version; }
 @Override public int hashCode() { return Objects.hash(servicioId,version); }
}