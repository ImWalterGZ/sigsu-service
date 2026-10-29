package mx.utch.sigsu.catalogo.service;

import java.util.*;
import java.time.Instant;
import mx.utch.sigsu.catalogo.dto.*;
import mx.utch.sigsu.compartido.dto.ErrorCampo;
public interface ICatalogo {
 List<ServicioDto> servicios();
 ServicioDto obtener(UUID id);
 CondicionesDto condiciones(UUID servicioId, Instant ahora);
 List<ErrorCampo> validar(UUID servicioId, int version, Map<String,Object> datos);
}