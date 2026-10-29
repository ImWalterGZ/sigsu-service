package mx.utch.sigsu.administracion.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import mx.utch.sigsu.seguridad.service.IUsuarios;
import mx.utch.sigsu.seguridad.dto.*;
@Service
public class GestionarUsuarios {
 private final IUsuarios usuarios;
 public GestionarUsuarios(IUsuarios usuarios) { this.usuarios=usuarios; }
 public UsuarioDto crear(CrearUsuarioDto datos) { return usuarios.crear(datos); }
 public void asignar(UUID usuario,UUID area) { usuarios.asignar(usuario,area); }
}