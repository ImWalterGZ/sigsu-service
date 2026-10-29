package mx.utch.sigsu.seguridad.service;

import mx.utch.sigsu.compartido.BusinessException;
import mx.utch.sigsu.seguridad.dto.*;
import mx.utch.sigsu.seguridad.model.*;
import mx.utch.sigsu.seguridad.repository.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Usuarios implements IUsuarios {
    private final UsuarioRepositorio usuarios;
    private final ResponsableAreaRepositorio responsables;
    private final PasswordEncoder contrasenas;

    public Usuarios(
            UsuarioRepositorio usuarios,
            ResponsableAreaRepositorio responsables,
            PasswordEncoder contrasenas) {
        this.usuarios = usuarios;
        this.responsables = responsables;
        this.contrasenas = contrasenas;
    }

    public UsuarioDto obtener(UUID id) {
        return dto(
                usuarios.findById(id)
                        .orElseThrow(() -> new BusinessException(404, "usuario inexistente")));
    }

    public Set<UUID> areas(UUID usuarioId) {
        return responsables.findByUsuarioId(usuarioId).stream()
                .map(a -> a.areaId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public UsuarioDto crear(CrearUsuarioDto datos) {
        Usuario usuario = new Usuario();
        usuario.id = UUID.randomUUID();
        usuario.correo = datos.correo();
        usuario.nombre = datos.nombre();
        usuario.contrasena = contrasenas.encode(datos.contrasena());
        usuario.activo = true;
        usuario.roles = datos.roles().stream().map(Rol::valueOf).collect(Collectors.toSet());
        return dto(usuarios.saveAndFlush(usuario));
    }

    @Transactional
    public void asignar(UUID usuarioId, UUID areaId) {
        Usuario usuario =
                usuarios.findById(usuarioId)
                        .orElseThrow(() -> new BusinessException(404, "usuario inexistente"));
        if (!usuario.roles.contains(Rol.RESPONSABLE))
            throw new BusinessException(422, "el usuario necesita rol RESPONSABLE");
        ResponsableArea responsable = new ResponsableArea();
        responsable.id = UUID.randomUUID();
        responsable.usuarioId = usuarioId;
        responsable.areaId = areaId;
        responsable.asignadaEn = Instant.now();
        responsables.saveAndFlush(responsable);
    }

    private UsuarioDto dto(Usuario usuario) {
        return new UsuarioDto(
                usuario.id,
                usuario.correo,
                usuario.nombre,
                usuario.roles.stream().map(Enum::name).collect(Collectors.toSet()),
                usuario.activo);
    }
}
