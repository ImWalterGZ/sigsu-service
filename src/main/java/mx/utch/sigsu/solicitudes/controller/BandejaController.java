package mx.utch.sigsu.solicitudes.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import mx.utch.sigsu.compartido.UsuarioActual;
import mx.utch.sigsu.solicitudes.model.Estado;
import mx.utch.sigsu.solicitudes.dto.SolicitudDto;
import mx.utch.sigsu.solicitudes.service.ConsultarSolicitudes;
@RestController @RequestMapping("/bandeja")
public class BandejaController {
 private final ConsultarSolicitudes consultas;
 public BandejaController(ConsultarSolicitudes consultas) { this.consultas=consultas; }
 @GetMapping public List<SolicitudDto> bandeja(Authentication autenticacion,@RequestParam(required=false) Estado estado) { return consultas.bandeja(UsuarioActual.de(autenticacion),estado); }
}