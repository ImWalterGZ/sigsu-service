# SIGSU — servicio de solicitudes universitarias

Prototipo funcional de la UTCH, Arquitectura de Software, Evidencia 4. Monolito modular con Spring Boot 4.1.1, Java 21, Spring MVC, JPA, PostgreSQL 16, Flyway, Spring Security HTTP Basic, Redis 7, JSON Schema draft 2020-12 y SMTP. Swagger UI: <http://localhost:8080/swagger-ui/index.html>.

## Ejecutar

```bash
podman compose up -d --build
# Alternativa compatible:
docker compose up -d --build
curl -s http://localhost:8080/actuator/health
```

La aplicación publica el puerto 8080, PostgreSQL 5432, Redis 6379 y Mailpit 8025. Para cambiar puertos:

```bash
SIGSU_PUERTO_APP=18080 SIGSU_PUERTO_POSTGRES=15432 \
SIGSU_PUERTO_REDIS=16379 SIGSU_PUERTO_MAILPIT=18025 podman compose up -d --build
```

Se usan imágenes con nombre completo y volúmenes nombrados para PostgreSQL y adjuntos, sin bind mounts que requieran etiquetas SELinux. La aplicación corre como usuario `spring`. El arranque espera los healthchecks de PostgreSQL, Redis y Mailpit. Los datos sobreviven al reinicio. `podman compose down` detiene el conjunto; agregar `-v` también **borra sus datos**.

```bash
# Programación de vencimientos cada minuto (normal: cada 15 minutos):
SIGSU_VENCIMIENTOS_CRON='0 * * * * *' podman compose up -d
podman compose logs app
curl -s http://localhost:8025/api/v1/messages
```

Para ejecutar sin contenedor, instalar un **JDK** 21 o posterior, PostgreSQL, Redis y SMTP; configurar las variables de `application.yaml` y ejecutar `./mvnw spring-boot:run`. El puerto SMTP local es 1025; Compose lo conecta internamente a Mailpit. Las pruebas no requieren estos servicios:

```bash
./mvnw -q test
```

Java compila con `release 21` aunque el JDK usado sea posterior. La máquina de verificación tenía Java 25 sin el compilador completo; se usó temporalmente un JDK Temurin 21 (`export JAVA_HOME=/tmp/sigsu-jdk`). No es una ruta requerida por el proyecto.

## Usuarios de prueba

Contraseña de todos: `sigsu123`. Son datos locales de demostración, insertados mediante Flyway V2 con BCrypt.

| Correo | UUID (últimos dígitos) | Roles / área |
|---|---|---|
| estudiante1@alumnos.utch.edu.mx | 101 | ESTUDIANTE |
| estudiante2@alumnos.utch.edu.mx | 102 | ESTUDIANTE |
| resp.escolares@utch.edu.mx | 103 | RESPONSABLE / Servicios Escolares |
| resp.escolares2@utch.edu.mx | 104 | RESPONSABLE / Servicios Escolares |
| resp.ti@utch.edu.mx | 105 | RESPONSABLE / Tecnologías de la Información |
| resp.materiales@utch.edu.mx | 106 | RESPONSABLE / Recursos Materiales |
| admin@utch.edu.mx | 107 | ADMINISTRADOR |
| profe.posgrado@utch.edu.mx | 108 | ESTUDIANTE y RESPONSABLE / Recursos Materiales |

Los UUID completos tienen la forma `00000000-0000-0000-0000-000000000101`. Áreas: terminaciones 001, 002 y 003; categorías: 011, 012 y 013. Servicios: 021 (constancia, 3 días hábiles), 022 (contraseña, 1 día hábil), 023 (laboratorio, 2 días hábiles).

## Recorrido con curl

Estas variables se reutilizan en los ejemplos. Sustituir `FOLIO`, `ADJUNTO`, `SERVICIO_NUEVO` y `USUARIO_NUEVO` por los valores devueltos. Los ejemplos de cierre y cancelación requieren solicitudes diferentes: una solicitud cerrada ya no puede transitar de nuevo.

```bash
BASE=http://localhost:8080
ESTUDIANTE=estudiante1@alumnos.utch.edu.mx:sigsu123
RESPONSABLE=resp.escolares@utch.edu.mx:sigsu123
ADMIN=admin@utch.edu.mx:sigsu123
SERVICIO=00000000-0000-0000-0000-000000000021
FOLIO=SIGSU-2026-000001
```

### Salud y documentación

```bash
curl "$BASE/actuator/health"
curl "$BASE/v3/api-docs"
curl -L "$BASE/swagger-ui/index.html"
```

### Catálogo

```bash
curl -u "$ESTUDIANTE" "$BASE/catalogo/servicios"
curl -u "$ESTUDIANTE" "$BASE/catalogo/servicios/$SERVICIO"
```

La lista incluye servicios activos. El detalle muestra la versión vigente, el esquema, el área y el plazo.

### Registrar y consultar solicitudes

```bash
curl -i -u "$ESTUDIANTE" -H 'Content-Type: application/json' \
  -d '{"servicioId":"00000000-0000-0000-0000-000000000021","datos":{"motivo":"Para solicitar una beca","conCalificaciones":true}}' \
  "$BASE/solicitudes"
curl -u "$ESTUDIANTE" "$BASE/solicitudes"
curl -u "$ESTUDIANTE" "$BASE/solicitudes/$FOLIO"
```

El registro responde 201 con folio, estado, versión y `venceEn`. La hora se conserva en America/Chihuahua, saltando sábados, domingos y días inhábiles. Un formulario inválido responde 422 con `errores: [{campo,mensaje}]`; un duplicado responde 409 con `folioExistente`. El detalle incluye historial y metadatos de adjuntos; comprueba propiedad, área o rol administrador.

Los otros formularios de la semilla se envían así:

```bash
curl -u "$ESTUDIANTE" -H 'Content-Type: application/json' \
  -d '{"servicioId":"00000000-0000-0000-0000-000000000022","datos":{}}' "$BASE/solicitudes"
curl -u "$ESTUDIANTE" -H 'Content-Type: application/json' \
  -d '{"servicioId":"00000000-0000-0000-0000-000000000023","datos":{"laboratorio":"Software","fecha":"2026-11-06","horario":"10:00-12:00","asistentes":20}}' "$BASE/solicitudes"
```

### Bandeja, toma, cierre y cancelación

```bash
curl -u "$RESPONSABLE" "$BASE/bandeja"
curl -u "$RESPONSABLE" "$BASE/bandeja?estado=REGISTRADA"
curl -i -u "$RESPONSABLE" -H 'Content-Type: application/json' \
  -d '{"version":0}' "$BASE/solicitudes/$FOLIO/tomar"
curl -u "$RESPONSABLE" -H 'Content-Type: application/json' \
  -d '{"estado":"ATENDIDA","observacion":"Constancia entregada"}' "$BASE/solicitudes/$FOLIO/estado"
# Alternativa de cierre; la observación es obligatoria para RECHAZADA:
curl -u "$RESPONSABLE" -H 'Content-Type: application/json' \
  -d '{"estado":"RECHAZADA","observacion":"Falta acreditar inscripción"}' "$BASE/solicitudes/$FOLIO/estado"
# Otra solicitud, todavía REGISTRADA:
curl -u "$ESTUDIANTE" -X POST "$BASE/solicitudes/$FOLIO/cancelar"
```

La bandeja contiene solicitudes abiertas de las áreas asignadas, ordenadas por vencimiento. La toma requiere la versión leída: una segunda toma con esa misma versión responde 409. Tomar SIN_RESPUESTA produce DEMORADO. Solo quien tomó puede cerrar; solo el estudiante dueño puede cancelar y únicamente desde REGISTRADA.

### Adjuntos

```bash
printf 'Documento de prueba\n' > /tmp/documento-sigsu.txt
curl -u "$ESTUDIANTE" -F archivo=@/tmp/documento-sigsu.txt "$BASE/solicitudes/$FOLIO/adjuntos"
ADJUNTO=uuid-devuelto
curl -u "$ESTUDIANTE" -o /tmp/descarga-sigsu.txt "$BASE/solicitudes/$FOLIO/adjuntos/$ADJUNTO"
```

La carga devuelve 201. Máximo 5 MB por archivo; nombre físico UUID, descarga como attachment y tipo binario. Los adjuntos se suben después del registro JSON: es una diferencia explícita frente a la secuencia original del diseño.

### Administración de catálogo

```bash
curl -u "$ADMIN" -H 'Content-Type: application/json' \
  -d '{"nombre":"Constancia de horario","descripcion":"Horario del estudiante","areaId":"00000000-0000-0000-0000-000000000001","categoriaId":"00000000-0000-0000-0000-000000000011","plazoDiasHabiles":2,"esquema":{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","additionalProperties":false,"required":[],"properties":{}}}' \
  "$BASE/administracion/servicios"
SERVICIO_NUEVO=uuid-devuelto
curl -u "$ADMIN" -H 'Content-Type: application/json' \
  -d '{"esquema":{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","additionalProperties":false,"required":["motivo"],"properties":{"motivo":{"type":"string","minLength":5}}}}' \
  "$BASE/administracion/servicios/$SERVICIO_NUEVO/formularios"
curl -u "$ADMIN" -X PATCH -H 'Content-Type: application/json' \
  -d '{"plazoDiasHabiles":4,"activo":false}' "$BASE/administracion/servicios/$SERVICIO_NUEVO"
curl -u "$ADMIN" "$BASE/administracion/dias-inhabiles"
curl -u "$ADMIN" -H 'Content-Type: application/json' \
  -d '{"fecha":"2026-12-24","motivo":"Receso escolar"}' "$BASE/administracion/dias-inhabiles"
```

Crear servicio y publicar formulario responden 201. Publicar inserta una versión nueva; las solicitudes anteriores conservan su versión y su vencimiento. Los cambios de plazo solo afectan registros posteriores. Agregar un día ya existente reemplaza su motivo.

### Administración de usuarios y parámetros

```bash
curl -u "$ADMIN" -H 'Content-Type: application/json' \
  -d '{"correo":"nuevo@utch.edu.mx","nombre":"Responsable nuevo","contrasena":"sigsu123","roles":["ESTUDIANTE","RESPONSABLE"]}' \
  "$BASE/administracion/usuarios"
USUARIO_NUEVO=uuid-devuelto
curl -i -u "$ADMIN" -H 'Content-Type: application/json' \
  -d '{"areaId":"00000000-0000-0000-0000-000000000003"}' "$BASE/administracion/usuarios/$USUARIO_NUEVO/areas"
curl -u "$ADMIN" "$BASE/administracion/parametros"
curl -u "$ADMIN" -X PUT -H 'Content-Type: application/json' \
  -d '{"valor":"sigsu@utch.edu.mx"}' "$BASE/administracion/parametros/correo.remitente"
```

Crear usuario devuelve 201 y asignar área devuelve 204. Los parámetros son valores administrables del prototipo; la zona del cálculo está fijada por el requisito y el remitente SMTP se configura al arrancar (ver notas).

### Vencimientos manuales

```bash
curl -u "$ADMIN" -X POST "$BASE/administracion/vencimientos"
curl -u "$ADMIN" -X POST "$BASE/administracion/vencimientos?ahora=2027-02-01T12:00:00Z"
```

Devuelve conteos y folios por transición a SIN_RESPUESTA y DEMORADO. Cada fila genera historial con autor nulo y observación `plazo vencido`, además del evento de correo. El `ahora` futuro es una facilidad de demostración que sí cambia los datos.

## Idempotencia y duplicados: reglas distintas

`Idempotency-Key` protege los reintentos del **mismo request**, ligados al correo autenticado. Conserva status, Content-Type y cuerpo durante 24 horas por defecto (`SIGSU_IDEMPOTENCIA_TTL`). La huella incluye método, ruta, query string y bytes del cuerpo. Reintentar produce exactamente la respuesta guardada y `Idempotent-Replayed: true`. Si la primera petición sigue en proceso, devuelve 409; si se reutiliza la llave con otra petición, 422. Un fallo 5xx libera la llave. No enviar el header deja la ejecución normal.

```bash
for intento in 1 2; do
  curl -i -u "$ESTUDIANTE" -H 'Idempotency-Key: constancia-001' \
    -H 'Content-Type: application/json' \
    -d '{"servicioId":"00000000-0000-0000-0000-000000000021","datos":{"motivo":"Para solicitar una beca","conCalificaciones":true}}' "$BASE/solicitudes"
done
```

El **índice parcial** protege la regla de negocio entre **dos solicitudes distintas**: no se permiten dos abiertas del mismo servicio para el mismo estudiante, incluso con llaves distintas o sin Redis. No hay consulta previa al INSERT. Solo se traduce en duplicado la violación 23505 de `solicitud_abierta`, consultando el folio después del rollback. Al cerrar se libera esa combinación.

En multipart la frontera MIME también forma parte del cuerpo: repetir `curl -F` genera fronteras distintas. Para un reintento idéntico hay que conservar el cuerpo multipart original. El script de verificación lo demuestra.

## Verificar y explorar

```bash
./mvnw -q test
python3 scripts/verificar.py
podman compose exec postgres psql -U sigsu -d sigsu -c "UPDATE historial SET observacion='x'"
# ERROR: historial es de solo inserción
```

El script usa curl y usuarios nuevos, agrega datos y **no borra** lo anterior. Verifica permisos, formularios, duplicados, bandeja, toma simultánea, cierres, adjuntos, idempotencia JSON y multipart, versionado, administración, vencimientos, Mailpit y triggers. Escribe los resultados reales en [docs/VERIFICACION.md](docs/VERIFICACION.md). Admite `SIGSU_URL` y `SIGSU_MAILPIT_URL` si se cambian los puertos; los comandos psql usan el proyecto Compose local.

Las decisiones y diferencias frente al diseño están en [docs/NOTAS-IMPLEMENTACION.md](docs/NOTAS-IMPLEMENTACION.md). No se incluyen colección Postman ni documentos de trazabilidad en esta fase.
