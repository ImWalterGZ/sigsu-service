package mx.utch.sigsu.catalogo.service;

import java.util.*;
import mx.utch.sigsu.catalogo.dto.*;
public interface IGestionCatalogo {
 ServicioDto crear(CrearServicioDto datos);
 ServicioDto publicar(UUID id, FormularioDto datos);
 ServicioDto ajustar(UUID id, AjustarServicioDto datos);
 List<DiaInhabilDto> diasInhabiles();
 DiaInhabilDto agregarDia(DiaInhabilDto datos);
}