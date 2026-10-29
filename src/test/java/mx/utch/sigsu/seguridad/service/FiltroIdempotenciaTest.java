package mx.utch.sigsu.seguridad.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.*;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

class FiltroIdempotenciaTest {
    private Map<String, String> valores;
    private FiltroIdempotencia filtro;
    private StringRedisTemplate redis;
    private ValueOperations<String, String> operaciones;
    private final String clave = "sigsu:idem:estudiante@utch.edu.mx:abc";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void preparar() {
        valores = new HashMap<>();
        redis = mock(StringRedisTemplate.class);
        operaciones = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(operaciones);
        when(operaciones.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenAnswer(i -> valores.putIfAbsent(i.getArgument(0), i.getArgument(1)) == null);
        when(operaciones.get(anyString())).thenAnswer(i -> valores.get(i.getArgument(0)));
        doAnswer(
                        i -> {
                            valores.put(i.getArgument(0), i.getArgument(1));
                            return null;
                        })
                .when(operaciones)
                .set(anyString(), anyString(), any(Duration.class));
        doAnswer(i -> valores.remove(i.getArgument(0)) != null).when(redis).delete(anyString());
        filtro = new FiltroIdempotencia(redis, Duration.ofHours(24), JsonMapper.builder().build());
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "estudiante@utch.edu.mx", "clave", List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest peticion(String cuerpo) {
        var peticion = new MockHttpServletRequest("POST", "/solicitudes");
        peticion.addHeader("Idempotency-Key", "abc");
        peticion.setContentType("application/json");
        peticion.setContent(cuerpo.getBytes(StandardCharsets.UTF_8));
        return peticion;
    }

    @Test
    void primeraGuardaYReintentoReproduceExactamente() throws Exception {
        var primera = new MockHttpServletResponse();
        filtro.doFilter(
                peticion("{}"),
                primera,
                (peticion, respuesta) -> {
                    assertEquals(
                            "{}",
                            new String(
                                    peticion.getInputStream().readAllBytes(),
                                    StandardCharsets.UTF_8));
                    respuesta.setContentType("application/json");
                    ((jakarta.servlet.http.HttpServletResponse) respuesta).setStatus(201);
                    respuesta
                            .getOutputStream()
                            .write(
                                    "{\"folio\":\"SIGSU-2026-000001\"}"
                                            .getBytes(StandardCharsets.UTF_8));
                });
        var segunda = new MockHttpServletResponse();
        filtro.doFilter(peticion("{}"), segunda, (p, r) -> fail("no debe volver a ejecutar"));
        assertEquals(201, segunda.getStatus());
        assertEquals(primera.getContentType(), segunda.getContentType());
        assertArrayEquals(primera.getContentAsByteArray(), segunda.getContentAsByteArray());
        assertEquals("true", segunda.getHeader("Idempotent-Replayed"));
        assertTrue(valores.get(clave).contains("COMPLETADA"));
    }

    @Test
    void procesandoDevuelve409() throws Exception {
        String huella =
                FiltroIdempotencia.huella(
                        "POST", "/solicitudes", "{}".getBytes(StandardCharsets.UTF_8));
        valores.put(
                clave, "{\"estado\":\"PROCESANDO\",\"huella\":\"" + huella + "\",\"status\":0}");
        var respuesta = new MockHttpServletResponse();
        filtro.doFilter(peticion("{}"), respuesta, (p, r) -> fail());
        assertEquals(409, respuesta.getStatus());
    }

    @Test
    void huellaDistintaDevuelve422() throws Exception {
        valores.put(clave, "{\"estado\":\"PROCESANDO\",\"huella\":\"otra\",\"status\":0}");
        var respuesta = new MockHttpServletResponse();
        filtro.doFilter(peticion("{}"), respuesta, (p, r) -> fail());
        assertEquals(422, respuesta.getStatus());
    }

    @Test
    void error500LiberaLaLlave() throws Exception {
        var respuesta = new MockHttpServletResponse();
        filtro.doFilter(
                peticion("{}"),
                respuesta,
                (p, r) -> ((jakarta.servlet.http.HttpServletResponse) r).setStatus(500));
        assertFalse(valores.containsKey(clave));
    }

    @Test
    void excepcionLiberaLaLlave() {
        assertThrows(
                jakarta.servlet.ServletException.class,
                () ->
                        filtro.doFilter(
                                peticion("{}"),
                                new MockHttpServletResponse(),
                                (p, r) -> {
                                    throw new jakarta.servlet.ServletException("fallo");
                                }));
        assertFalse(valores.containsKey(clave));
    }

    @Test
    void sinHeaderContinuaSinRedis() throws Exception {
        var peticion = peticion("{}");
        peticion.removeHeader("Idempotency-Key");
        filtro.doFilter(peticion, new MockHttpServletResponse(), (p, r) -> {});
        verifyNoInteractions(redis);
    }
}
