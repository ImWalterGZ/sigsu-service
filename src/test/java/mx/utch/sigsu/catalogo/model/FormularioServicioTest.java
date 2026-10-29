package mx.utch.sigsu.catalogo.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.*;

class FormularioServicioTest {
    private FormularioServicio formulario() {
        FormularioServicio formulario = new FormularioServicio();
        formulario.esquema =
                Map.of(
                        "$schema",
                        "https://json-schema.org/draft/2020-12/schema",
                        "type",
                        "object",
                        "additionalProperties",
                        false,
                        "required",
                        List.of("motivo"),
                        "properties",
                        Map.of(
                                "motivo",
                                Map.of("type", "string", "minLength", 5),
                                "correo",
                                Map.of("type", "string", "format", "email")));
        return formulario;
    }

    @Test
    void aceptaDatosValidos() {
        assertTrue(formulario().validar(Map.of("motivo", "Para una beca")).isEmpty());
    }

    @Test
    void devuelveCamposInvalidos() {
        var errores = formulario().validar(Map.of("motivo", "no", "extra", true));
        assertEquals(2, errores.size());
        assertTrue(errores.stream().anyMatch(e -> e.campo().contains("motivo")));
    }

    @Test
    void exigeObligatoriosYFormato() {
        assertFalse(formulario().validar(Map.of()).isEmpty());
        assertFalse(
                formulario()
                        .validar(Map.of("motivo", "Para beca", "correo", "invalido"))
                        .isEmpty());
    }
}
