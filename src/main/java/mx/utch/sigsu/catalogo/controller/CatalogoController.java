package mx.utch.sigsu.catalogo.controller;

import mx.utch.sigsu.catalogo.dto.ServicioDto;
import mx.utch.sigsu.catalogo.service.ICatalogo;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/catalogo/servicios")
public class CatalogoController {
    private final ICatalogo catalogo;

    public CatalogoController(ICatalogo catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    public List<ServicioDto> listar() {
        return catalogo.servicios();
    }

    @GetMapping("/{id}")
    public ServicioDto obtener(@PathVariable UUID id) {
        return catalogo.obtener(id);
    }
}
