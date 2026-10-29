package mx.utch.sigsu.solicitudes.model;

import java.util.Set;

public enum Estado {
    REGISTRADA,
    SIN_RESPUESTA,
    EN_ATENCION,
    DEMORADO,
    ATENDIDA,
    RECHAZADA,
    CANCELADA;
    public static final Set<Estado> ABIERTOS =
            Set.of(REGISTRADA, SIN_RESPUESTA, EN_ATENCION, DEMORADO);
}
