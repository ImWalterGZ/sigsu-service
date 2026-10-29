package mx.utch.sigsu.administracion.controller;

import jakarta.validation.Valid;

import mx.utch.sigsu.administracion.dto.*;
import mx.utch.sigsu.administracion.service.*;
import mx.utch.sigsu.catalogo.dto.*;
import mx.utch.sigsu.catalogo.service.IGestionCatalogo;
import mx.utch.sigsu.seguridad.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/administracion")
public class AdminController {
    private final IGestionCatalogo catalogo;
    private final GestionarUsuarios usuarios;
    private final GestionarParametros parametros;

    public AdminController(
            IGestionCatalogo catalogo,
            GestionarUsuarios usuarios,
            GestionarParametros parametros) {
        this.catalogo = catalogo;
        this.usuarios = usuarios;
        this.parametros = parametros;
    }

    @PostMapping("/servicios")
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioDto crearServicio(@Valid @RequestBody CrearServicioDto datos) {
        return catalogo.crear(datos);
    }

    @PostMapping("/servicios/{id}/formularios")
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioDto formulario(@PathVariable UUID id, @Valid @RequestBody FormularioDto datos) {
        return catalogo.publicar(id, datos);
    }

    @PatchMapping("/servicios/{id}")
    public ServicioDto ajustar(
            @PathVariable UUID id, @Valid @RequestBody AjustarServicioDto datos) {
        return catalogo.ajustar(id, datos);
    }

    @GetMapping("/dias-inhabiles")
    public List<DiaInhabilDto> dias() {
        return catalogo.diasInhabiles();
    }

    @PostMapping("/dias-inhabiles")
    @ResponseStatus(HttpStatus.CREATED)
    public DiaInhabilDto agregarDia(@Valid @RequestBody DiaInhabilDto datos) {
        return catalogo.agregarDia(datos);
    }

    @GetMapping("/parametros")
    public Map<String, String> parametros() {
        return parametros.listar();
    }

    @PutMapping("/parametros/{clave}")
    public ParametroDto parametro(
            @PathVariable String clave, @Valid @RequestBody ParametroDto datos) {
        return parametros.cambiar(clave, datos);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto usuario(@Valid @RequestBody CrearUsuarioDto datos) {
        return usuarios.crear(datos);
    }

    @PostMapping("/usuarios/{id}/areas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void asignar(@PathVariable UUID id, @Valid @RequestBody AsignarResponsableDto datos) {
        usuarios.asignar(id, datos.areaId());
    }
}
