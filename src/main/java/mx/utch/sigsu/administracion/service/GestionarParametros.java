package mx.utch.sigsu.administracion.service;

import java.util.*;
import org.springframework.stereotype.Service;
import mx.utch.sigsu.administracion.repository.ParametroRepositorio;
import mx.utch.sigsu.administracion.dto.ParametroDto;
import mx.utch.sigsu.administracion.model.Parametro;
@Service
public class GestionarParametros {
 private final ParametroRepositorio parametros;
 public GestionarParametros(ParametroRepositorio parametros) { this.parametros=parametros; }
 public Map<String,String> listar() { Map<String,String> valores=new TreeMap<>(); parametros.findAll().forEach(p->valores.put(p.clave,p.valor)); return valores; }
 public ParametroDto cambiar(String clave,ParametroDto datos) { Parametro parametro=new Parametro(); parametro.clave=clave; parametro.valor=datos.valor(); parametros.saveAndFlush(parametro); return datos; }
}