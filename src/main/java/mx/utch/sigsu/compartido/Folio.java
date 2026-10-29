package mx.utch.sigsu.compartido;

import java.time.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class Folio {
 private final JdbcTemplate consultas;
 public Folio(JdbcTemplate consultas) { this.consultas=consultas; }
 public String siguiente(Instant ahora) {
  long numero=consultas.queryForObject("select nextval('secuencia_folio')",Long.class);
  return "SIGSU-%d-%06d".formatted(ahora.atZone(ZoneId.of("America/Chihuahua")).getYear(),numero);
 }
}