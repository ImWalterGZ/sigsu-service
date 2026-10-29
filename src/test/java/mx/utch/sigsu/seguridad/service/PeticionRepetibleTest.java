package mx.utch.sigsu.seguridad.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

class PeticionRepetibleTest {
    @Test
    void multipartConservaCuerpoYPartes() throws Exception {
        String cuerpo =
                "--limite\r\n"
                    + "Content-Disposition: form-data; name=\"archivo\"; filename=\"nota.txt\"\r\n"
                    + "Content-Type: text/plain\r\n\r\n"
                    + "Evidencia\r\n"
                    + "--limite--\r\n";
        var original = new MockHttpServletRequest("POST", "/solicitudes/folio/adjuntos");
        original.setContentType("multipart/form-data; boundary=limite");
        original.setContent(cuerpo.getBytes(StandardCharsets.UTF_8));
        var repetible = new PeticionRepetible(original);
        assertArrayEquals(
                cuerpo.getBytes(StandardCharsets.UTF_8), repetible.getInputStream().readAllBytes());
        assertEquals(
                "Evidencia",
                new String(
                        repetible.getPart("archivo").getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8));
        assertEquals("nota.txt", repetible.getPart("archivo").getSubmittedFileName());
        assertArrayEquals(
                cuerpo.getBytes(StandardCharsets.UTF_8), repetible.getInputStream().readAllBytes());
    }
}
