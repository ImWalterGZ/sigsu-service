package mx.utch.sigsu.solicitudes.controller;

import mx.utch.sigsu.solicitudes.dto.VencimientosDto;
import mx.utch.sigsu.solicitudes.service.VencerSolicitudes;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;

// Ejecución manual del proceso de vencimientos para demostrarlo sin esperar al @Scheduled.
// Vive en solicitudes para que administracion no dependa de este módulo; la ruta queda bajo
// /administracion para que la misma regla de seguridad exija ADMINISTRADOR.
@RestController
@RequestMapping("/administracion/vencimientos")
public class VencimientosController {
    private final VencerSolicitudes vencimientos;

    public VencimientosController(VencerSolicitudes vencimientos) {
        this.vencimientos = vencimientos;
    }

    @PostMapping
    public VencimientosDto vencer(@RequestParam(required = false) Instant ahora) {
        return vencimientos.vencer(ahora == null ? Instant.now() : ahora);
    }
}
