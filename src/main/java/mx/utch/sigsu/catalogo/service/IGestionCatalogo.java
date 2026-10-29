package mx.utch.sigsu.catalogo.service;

import mx.utch.sigsu.catalogo.dto.*;

import java.util.*;

public interface IGestionCatalogo {
    ServicioDto crear(CrearServicioDto datos);

    ServicioDto publicar(UUID id, FormularioDto datos);

    ServicioDto ajustar(UUID id, AjustarServicioDto datos);

    List<DiaInhabilDto> diasInhabiles();

    DiaInhabilDto agregarDia(DiaInhabilDto datos);
}
