package mx.utch.sigsu.seguridad.service;

import mx.utch.sigsu.compartido.*;
import mx.utch.sigsu.seguridad.repository.UsuarioRepositorio;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class Autenticar implements UserDetailsService {
    private final UsuarioRepositorio usuarios;

    public Autenticar(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) {
        var usuario =
                usuarios.findByCorreo(correo)
                        .orElseThrow(() -> new UsernameNotFoundException("credenciales inválidas"));
        var actual =
                new UsuarioActual(
                        usuario.id,
                        usuario.correo,
                        usuario.nombre,
                        usuario.roles.stream().map(Enum::name).collect(Collectors.toSet()));
        return new UsuarioAutenticado(actual, usuario.contrasena, usuario.activo);
    }

    private static class UsuarioAutenticado extends User implements PrincipalUsuario {
        private final UsuarioActual actual;

        UsuarioAutenticado(UsuarioActual actual, String contrasena, boolean activo) {
            super(
                    actual.correo(),
                    contrasena,
                    activo,
                    true,
                    true,
                    true,
                    actual.roles().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .toList());
            this.actual = actual;
        }

        public UsuarioActual usuario() {
            return actual;
        }
    }
}
