package mx.utch.sigsu.notificaciones.repository;

import mx.utch.sigsu.notificaciones.model.Aviso;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class CorreoSmtp {
    private final JavaMailSender correo;
    private final String remitente;

    public CorreoSmtp(
            JavaMailSender correo,
            @Value("${sigsu.correo.remitente:sigsu@utch.edu.mx}") String remitente) {
        this.correo = correo;
        this.remitente = remitente;
    }

    public void enviar(Aviso aviso) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(aviso.destinatarios().toArray(String[]::new));
        mensaje.setSubject(aviso.asunto());
        mensaje.setText(aviso.texto());
        correo.send(mensaje);
    }
}
