package mx.utch.sigsu.solicitudes.service;

import mx.utch.sigsu.compartido.*;
import mx.utch.sigsu.solicitudes.dto.*;
import mx.utch.sigsu.solicitudes.model.*;
import mx.utch.sigsu.solicitudes.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class TomarSolicitud {
    private final SolicitudRepositorio solicitudes;
    private final ConsultarSolicitudes consultas;
    private final RegistrarCambio cambios;

    public TomarSolicitud(
            SolicitudRepositorio solicitudes,
            ConsultarSolicitudes consultas,
            RegistrarCambio cambios) {
        this.solicitudes = solicitudes;
        this.consultas = consultas;
        this.cambios = cambios;
    }

    @Transactional
    public SolicitudDto tomar(String folio, UsuarioActual responsable, int version) {
        Solicitud solicitud = consultas.buscar(folio);
        consultas.exigirArea(responsable, solicitud);
        Estado anterior = solicitud.estado;
        if (anterior != Estado.REGISTRADA && anterior != Estado.SIN_RESPUESTA)
            throw new BusinessException(
                    409, "otro responsable la tomó primero o el estado no permite tomar");
        Estado nuevo = anterior == Estado.REGISTRADA ? Estado.EN_ATENCION : Estado.DEMORADO;
        if (solicitudes.tomar(solicitud.id, version, responsable.id(), nuevo.name()) == 0)
            throw new BusinessException(
                    409, "otro responsable la tomó primero o el estado no permite tomar");
        solicitud.tomar(responsable.id());
        solicitud.version = version + 1;
        cambios.registrar(
                solicitud, responsable.id(), anterior, "toma de solicitud", Instant.now());
        return SolicitudDto.de(solicitud);
    }
}
