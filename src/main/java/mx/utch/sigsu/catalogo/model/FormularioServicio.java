package mx.utch.sigsu.catalogo.model;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "formulario_servicio")
@IdClass(ClaveFormulario.class)
public class FormularioServicio {
    @Id public UUID servicioId;
    @Id public int version;

    @JdbcTypeCode(SqlTypes.JSON)
    public Map<String, Object> esquema;

    public Instant creadoEn;

    public FormularioServicio() {}

    public java.util.List<mx.utch.sigsu.compartido.dto.ErrorCampo> validar(
            Map<String, Object> datos) {
        var conversor = new com.fasterxml.jackson.databind.ObjectMapper();
        var configuracion =
                com.networknt.schema.SchemaValidatorsConfig.builder()
                        .locale(Locale.forLanguageTag("es"))
                        .formatAssertionsEnabled(true)
                        .build();
        var validador =
                com.networknt.schema.JsonSchemaFactory.getInstance(
                                com.networknt.schema.SpecVersion.VersionFlag.V202012)
                        .getSchema(conversor.valueToTree(esquema), configuracion);
        return validador.validate(conversor.valueToTree(datos)).stream()
                .map(
                        error ->
                                new mx.utch.sigsu.compartido.dto.ErrorCampo(
                                        error.getInstanceLocation().toString(), error.getMessage()))
                .toList();
    }
}
