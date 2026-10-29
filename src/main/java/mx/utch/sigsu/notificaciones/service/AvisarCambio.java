package mx.utch.sigsu.notificaciones.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.*;
import mx.utch.sigsu.compartido.CambioDeSolicitud;
import mx.utch.sigsu.notificaciones.model.Aviso;
import mx.utch.sigsu.notificaciones.repository.CorreoSmtp;
@Service
public class AvisarCambio {
 private final CorreoSmtp correo;
 public AvisarCambio(CorreoSmtp correo) { this.correo=correo; }
 @Async @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
 public void avisar(CambioDeSolicitud cambio) {
  try { correo.enviar(new Aviso(List.of(cambio.correoEstudiante(),cambio.correoArea()),cambio.folio()+": "+cambio.estadoNuevo(),"La solicitud "+cambio.folio()+" pasó de "+(cambio.estadoAnterior()==null?"sin registrar":cambio.estadoAnterior())+" a "+cambio.estadoNuevo()+" el "+cambio.fecha())); }
  catch(Exception error) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("falló aviso de {}",cambio.folio(),error); }
 }
}