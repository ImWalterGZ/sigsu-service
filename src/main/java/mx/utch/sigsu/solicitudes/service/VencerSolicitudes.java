package mx.utch.sigsu.solicitudes.service;

import java.util.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.utch.sigsu.solicitudes.model.*;
import mx.utch.sigsu.solicitudes.repository.*;
import mx.utch.sigsu.solicitudes.dto.*;
import mx.utch.sigsu.compartido.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.scheduling.annotation.Scheduled;
@Service
public class VencerSolicitudes {
 private final SolicitudRepositorio solicitudes;
 private final RegistrarCambio cambios;
 private final TransactionTemplate transacciones;
 public VencerSolicitudes(SolicitudRepositorio solicitudes, RegistrarCambio cambios, TransactionTemplate transacciones) { this.solicitudes=solicitudes; this.cambios=cambios; this.transacciones=transacciones; }

 @Scheduled(cron="${sigsu.vencimientos.cron:0 */15 * * * *}")
 public void programado() { vencer(Instant.now()); }
 public VencimientosDto vencer(Instant ahora) {
  return transacciones.execute(transaccion -> {
   List<String> sinRespuesta=new ArrayList<>(); List<String> demoradas=new ArrayList<>();
   for(Solicitud solicitud:solicitudes.vencidasBloqueadas(ahora)) {
    Estado anterior=solicitud.estado;
    if(solicitud.vencer(ahora)) {
     solicitudes.saveAndFlush(solicitud); cambios.registrar(solicitud,null,anterior,"plazo vencido",ahora);
     (solicitud.estado==Estado.SIN_RESPUESTA?sinRespuesta:demoradas).add(solicitud.folio);
    }
   }
   return new VencimientosDto(sinRespuesta.size(),demoradas.size(),sinRespuesta,demoradas);
  });
 }

}