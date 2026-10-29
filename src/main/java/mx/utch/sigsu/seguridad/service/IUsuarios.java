package mx.utch.sigsu.seguridad.service;

import mx.utch.sigsu.seguridad.dto.*;

import java.util.*;

public interface IUsuarios {
    UsuarioDto obtener(UUID id);

    Set<UUID> areas(UUID usuarioId);

    UsuarioDto crear(CrearUsuarioDto datos);

    void asignar(UUID usuarioId, UUID areaId);
}
