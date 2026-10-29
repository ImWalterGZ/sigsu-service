package mx.utch.sigsu.catalogo.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import mx.utch.sigsu.catalogo.service.ICatalogo;
import mx.utch.sigsu.catalogo.dto.ServicioDto;
@RestController @RequestMapping("/catalogo/servicios")
public class CatalogoController {
 private final ICatalogo catalogo;
 public CatalogoController(ICatalogo catalogo) { this.catalogo=catalogo; }
 @GetMapping public List<ServicioDto> listar() { return catalogo.servicios(); }
 @GetMapping("/{id}") public ServicioDto obtener(@PathVariable UUID id) { return catalogo.obtener(id); }
}