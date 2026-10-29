package mx.utch.sigsu.seguridad.service;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import mx.utch.sigsu.compartido.dto.ErrorResponseDto;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.util.*;

public class FiltroIdempotencia extends OncePerRequestFilter {
    private final StringRedisTemplate redis;
    private final Duration ttl;
    private final ObjectMapper json;

    public FiltroIdempotencia(StringRedisTemplate redis, Duration ttl, ObjectMapper json) {
        this.redis = redis;
        this.ttl = ttl;
        this.json = json;
    }

    public record RespuestaGuardada(
            String estado, String huella, int status, String contentType, byte[] cuerpo) {}

    @Override
    protected void doFilterInternal(
            HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String llave = peticion.getHeader("Idempotency-Key");
        if (!"POST".equals(peticion.getMethod()) || llave == null) {
            cadena.doFilter(peticion, respuesta);
            return;
        }
        if (llave.isBlank() || llave.length() > 200) {
            error(respuesta, 422, "Idempotency-Key debe tener entre 1 y 200 caracteres");
            return;
        }
        PeticionRepetible repetible;
        try {
            repetible = new PeticionRepetible(peticion);
        } catch (mx.utch.sigsu.compartido.BusinessException fallo) {
            error(respuesta, fallo.respuesta().status(), fallo.getMessage());
            return;
        }
        String ruta =
                peticion.getRequestURI()
                        + (peticion.getQueryString() == null
                                ? ""
                                : "?" + peticion.getQueryString());
        String huella = huella(peticion.getMethod(), ruta, repetible.cuerpo());
        String clave =
                "sigsu:idem:"
                        + SecurityContextHolder.getContext().getAuthentication().getName()
                        + ":"
                        + llave;
        var procesando = new RespuestaGuardada("PROCESANDO", huella, 0, null, null);
        boolean nueva =
                Boolean.TRUE.equals(
                        redis.opsForValue()
                                .setIfAbsent(clave, json.writeValueAsString(procesando), ttl));
        if (!nueva) {
            String valor = redis.opsForValue().get(clave);
            if (valor == null) {
                error(respuesta, 409, "Petición con esta Idempotency-Key en proceso");
                return;
            }
            RespuestaGuardada guardada = json.readValue(valor, RespuestaGuardada.class);
            if (!guardada.huella().equals(huella)) {
                error(respuesta, 422, "Idempotency-Key reutilizada con otra petición");
                return;
            }
            if ("PROCESANDO".equals(guardada.estado())) {
                error(respuesta, 409, "Petición con esta Idempotency-Key en proceso");
                return;
            }
            respuesta.setStatus(guardada.status());
            if (guardada.contentType() != null) respuesta.setContentType(guardada.contentType());
            respuesta.setHeader("Idempotent-Replayed", "true");
            respuesta.getOutputStream().write(guardada.cuerpo());
            return;
        }
        ContentCachingResponseWrapper copia = new ContentCachingResponseWrapper(respuesta);
        try {
            cadena.doFilter(repetible, copia);
            if (copia.getStatus() < 500)
                redis.opsForValue()
                        .set(
                                clave,
                                json.writeValueAsString(
                                        new RespuestaGuardada(
                                                "COMPLETADA",
                                                huella,
                                                copia.getStatus(),
                                                copia.getContentType(),
                                                copia.getContentAsByteArray())),
                                ttl);
            else redis.delete(clave);
        } catch (IOException | ServletException | RuntimeException error) {
            redis.delete(clave);
            throw error;
        } finally {
            copia.copyBodyToResponse();
        }
    }

    public static String huella(String metodo, String ruta, byte[] cuerpo) {
        try {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            resumen.update((metodo + "\n" + ruta + "\n").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen.digest(cuerpo));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    private void error(HttpServletResponse respuesta, int status, String mensaje)
            throws IOException {
        respuesta.setStatus(status);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta
                .getOutputStream()
                .write(
                        json.writeValueAsBytes(
                                new ErrorResponseDto(status, mensaje, null, List.of())));
    }
}
