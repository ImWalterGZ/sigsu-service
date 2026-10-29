package mx.utch.sigsu.catalogo.service;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.utch.sigsu.catalogo.model.*;
import mx.utch.sigsu.catalogo.repository.*;
import mx.utch.sigsu.catalogo.dto.*;
import mx.utch.sigsu.compartido.*;
import mx.utch.sigsu.compartido.dto.ErrorCampo;
@Service
public class Catalogo implements ICatalogo, IGestionCatalogo {
 private final ServicioRepositorio servicios; private final FormularioServicioRepositorio formularios; private final DiaInhabilRepositorio dias; private final AreaRepositorio areas;
 public Catalogo(ServicioRepositorio servicios, FormularioServicioRepositorio formularios, DiaInhabilRepositorio dias, AreaRepositorio areas) { this.servicios=servicios; this.formularios=formularios; this.dias=dias; this.areas=areas; }
 public List<ServicioDto> servicios() { return servicios.findAll().stream().filter(s->s.activo).map(this::dto).toList(); }
 public ServicioDto obtener(UUID id) { return dto(buscar(id)); }
 public CondicionesDto condiciones(UUID id, Instant ahora) {
  Servicio servicio=buscar(id); if(!servicio.activo) throw new BusinessException(422,"servicio inactivo");
  servicio.diasInhabiles=dias.findAll().stream().map(d->d.fecha).collect(Collectors.toSet());
  return new CondicionesDto(id,servicio.versionVigente,servicio.areaId,correo(servicio.areaId),formulario(id,servicio.versionVigente).esquema,servicio.calcularVencimiento(ahora));
 }
 public List<ErrorCampo> validar(UUID id,int version,Map<String,Object> datos) { return formulario(id,version).validar(datos); }
 @Transactional public ServicioDto crear(CrearServicioDto datos) {
  comprobarEsquema(datos.esquema());
  Servicio servicio=new Servicio(); servicio.id=UUID.randomUUID(); servicio.nombre=datos.nombre(); servicio.descripcion=datos.descripcion(); servicio.areaId=datos.areaId(); servicio.categoriaId=datos.categoriaId(); servicio.plazoDiasHabiles=datos.plazoDiasHabiles(); servicio.versionVigente=1; servicio.activo=true;
  servicios.saveAndFlush(servicio); insertarFormulario(servicio.id,1,datos.esquema()); return dto(servicio);
 }
 @Transactional public ServicioDto publicar(UUID id,FormularioDto datos) {
  comprobarEsquema(datos.esquema());
  Servicio servicio=buscar(id); servicio.versionVigente++; insertarFormulario(id,servicio.versionVigente,datos.esquema()); servicios.saveAndFlush(servicio); return dto(servicio);
 }
 @Transactional public ServicioDto ajustar(UUID id,AjustarServicioDto datos) { Servicio servicio=buscar(id); servicio.plazoDiasHabiles=datos.plazoDiasHabiles(); servicio.activo=datos.activo(); return dto(servicios.save(servicio)); }
 public List<DiaInhabilDto> diasInhabiles() { return dias.findAll().stream().sorted(Comparator.comparing(d->d.fecha)).map(d->new DiaInhabilDto(d.fecha,d.motivo)).toList(); }
 @Transactional public DiaInhabilDto agregarDia(DiaInhabilDto datos) { DiaInhabil dia=new DiaInhabil(); dia.fecha=datos.fecha(); dia.motivo=datos.motivo(); dias.saveAndFlush(dia); return datos; }
 private Servicio buscar(UUID id) { return servicios.findById(id).orElseThrow(()->new BusinessException(404,"servicio inexistente")); }
 private FormularioServicio formulario(UUID id,int version) { ClaveFormulario clave=new ClaveFormulario(); clave.servicioId=id; clave.version=version; return formularios.findById(clave).orElseThrow(()->new BusinessException(404,"formulario inexistente")); }
 private String correo(UUID area) { return areas.findById(area).orElseThrow(()->new BusinessException(404,"área inexistente")).correo; }
 private ServicioDto dto(Servicio servicio) { servicio.formulario=formulario(servicio.id,servicio.versionVigente); return new ServicioDto(servicio.id,servicio.nombre,servicio.descripcion,servicio.areaId,correo(servicio.areaId),servicio.categoriaId,servicio.plazoDiasHabiles,servicio.versionVigente,servicio.activo,servicio.formularioVigente().esquema); }
 private void insertarFormulario(UUID id,int version,Map<String,Object> esquema) { FormularioServicio formulario=new FormularioServicio(); formulario.servicioId=id; formulario.version=version; formulario.esquema=esquema; formulario.creadoEn=Instant.now(); formularios.saveAndFlush(formulario); }
 private void comprobarEsquema(Map<String,Object> esquema) {
  if(!"https://json-schema.org/draft/2020-12/schema".equals(esquema.get("$schema")) || !Boolean.FALSE.equals(esquema.get("additionalProperties")) || !"object".equals(esquema.get("type")) || !(esquema.get("required") instanceof List<?>)) throw new BusinessException(422,"se exige esquema de objeto draft 2020-12, required y additionalProperties:false");
  try { FormularioServicio formulario=new FormularioServicio(); formulario.esquema=esquema; formulario.validar(Map.of()); }
  catch(RuntimeException error) { throw new BusinessException(422,"JSON Schema inválido"); }
 }
}