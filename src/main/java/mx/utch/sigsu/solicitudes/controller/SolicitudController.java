package mx.utch.sigsu.solicitudes.controller;

import jakarta.validation.Valid;

import mx.utch.sigsu.compartido.UsuarioActual;
import mx.utch.sigsu.solicitudes.dto.*;
import mx.utch.sigsu.solicitudes.service.*;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {
    private final RegistrarSolicitud registro;
    private final ConsultarSolicitudes consultas;
    private final TomarSolicitud toma;
    private final CambiarEstado cambios;
    private final GestionarAdjuntos adjuntos;

    public SolicitudController(
            RegistrarSolicitud registro,
            ConsultarSolicitudes consultas,
            TomarSolicitud toma,
            CambiarEstado cambios,
            GestionarAdjuntos adjuntos) {
        this.registro = registro;
        this.consultas = consultas;
        this.toma = toma;
        this.cambios = cambios;
        this.adjuntos = adjuntos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudDto registrar(
            @Valid @RequestBody RegistrarSolicitudDto datos, Authentication autenticacion) {
        return registro.registrar(UsuarioActual.de(autenticacion), datos);
    }

    @GetMapping
    public List<SolicitudDto> propias(Authentication autenticacion) {
        return consultas.propias(UsuarioActual.de(autenticacion));
    }

    @GetMapping("/{folio}")
    public DetalleSolicitudDto detalle(@PathVariable String folio, Authentication autenticacion) {
        return consultas.detalle(folio, UsuarioActual.de(autenticacion));
    }

    @PostMapping("/{folio}/tomar")
    public SolicitudDto tomar(
            @PathVariable String folio,
            @Valid @RequestBody TomarSolicitudDto datos,
            Authentication autenticacion) {
        return toma.tomar(folio, UsuarioActual.de(autenticacion), datos.version());
    }

    @PostMapping("/{folio}/estado")
    public SolicitudDto cambiar(
            @PathVariable String folio,
            @Valid @RequestBody CambiarEstadoDto datos,
            Authentication autenticacion) {
        return cambios.cambiar(folio, UsuarioActual.de(autenticacion), datos);
    }

    @PostMapping("/{folio}/cancelar")
    public SolicitudDto cancelar(@PathVariable String folio, Authentication autenticacion) {
        return cambios.cancelar(folio, UsuarioActual.de(autenticacion));
    }

    @PostMapping(value = "/{folio}/adjuntos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AdjuntoDto adjuntar(
            @PathVariable String folio,
            @RequestPart("archivo") MultipartFile archivo,
            Authentication autenticacion) {
        return adjuntos.guardar(folio, UsuarioActual.de(autenticacion), archivo);
    }

    @GetMapping("/{folio}/adjuntos/{id}")
    public ResponseEntity<Resource> descargar(
            @PathVariable String folio, @PathVariable UUID id, Authentication autenticacion) {
        return adjuntos.descargar(folio, id, UsuarioActual.de(autenticacion));
    }
}
