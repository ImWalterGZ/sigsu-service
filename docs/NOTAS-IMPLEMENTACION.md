# Notas de implementación

## Alcance y relación con el diseño

Se respetan los nombres de clases del modelo y los módulos `seguridad`, `catalogo`, `solicitudes`, `notificaciones`, `administracion` y `compartido`. Se usan capas `controller`, `service`, `repository`, `model` y `dto` solo donde hacen falta. No se agrega arquitectura hexagonal, bus externo, cola ni adaptadores por interfaz para cada repositorio.

La especificación de esta fase autoriza estas diferencias frente al texto y diagramas originales:

- API REST con Spring MVC y HTTP Basic sin sesión, CSRF deshabilitado. Sustituye páginas generadas en servidor y cookie HttpOnly. `Autenticar` implementa `UserDetailsService`; no se crean `LoginController` ni `FiltroDeSesion` de la versión web original.
- Proceso `@Scheduled` dentro de la aplicación y ejecución manual administrativa, en lugar de Render Cron y argumento `--vencimientos`.
- Compose local con PostgreSQL, Redis y Mailpit; no hay despliegue en Render ni conexión con Microsoft 365.
- Trigger para historial y formularios en vez de permisos del usuario SQL: protege también al dueño de las tablas y rechaza UPDATE, DELETE y TRUNCATE. No se modificó la topología de datos del modelo.
- Registro JSON y carga posterior de adjuntos. Si falla la transacción de metadatos, se borra el archivo recién escrito; un corte abrupto del proceso entre escritura y commit puede dejar un archivo huérfano. No hay recolector de huérfanos en este prototipo.

## Dependencias y compilación

- Spring Boot **4.1.1**, Java **21**, Maven Wrapper copiado de `order-service` (Maven **3.10.0** según sus propiedades), Springdoc MVC **3.0.0**, conforme al estilo de los repositorios del profesor. Los starters JPA, seguridad, Redis, correo, Flyway, Actuator y pruebas toman sus versiones del BOM de Boot.
- Networknt `json-schema-validator` **1.5.9** ofrece la API `JsonSchemaFactory` y draft 2020-12 con Jackson 2. Se declara explícitamente `com.fasterxml.jackson.core:jackson-databind` y se deja su versión al BOM de Boot. MVC y Redis serializan con el `ObjectMapper` Jackson **3** (`tools.jackson`) que autoconfigura Boot. El validador convierte `Map<String,Object>` con un ObjectMapper Jackson **2** privado. No se intercambian JsonNode entre ambas familias.
- Se habilitan afirmaciones de `format` para comprobar email y date; se usa locale español. Las ubicaciones de error conservan la notación del validador, por ejemplo `$.motivo`. Fuentes consultadas: [repositorio oficial de networknt](https://github.com/networknt/json-schema-validator) y [API de 1.5.9](https://www.javadoc.io/static/com.networknt/json-schema-validator/1.5.9/com/networknt/schema/JsonSchemaFactory.html).
- Boot administra JUnit 6 por defecto. Se fija `junit-jupiter.version=5.13.4` para cumplir las pruebas JUnit 5 solicitadas, manteniendo Mockito 5.23.0 del BOM. Las versiones resueltas de JSON son Jackson 3.1.5 para MVC y Jackson 2.21.5 para networknt; Flyway es 12.4.0.
- El driver PostgreSQL tiene alcance de compilación porque se consulta `PSQLException.getServerErrorMessage().getConstraint()` para reconocer exclusivamente el índice `solicitud_abierta`, además de SQLState 23505. No se convierten otras restricciones únicas en duplicados de negocio.
- La máquina tiene runtime Java **25.0.4.1**, pero su instalación no incluye binario `javac` ni soporte completo para compilar con release 21. La primera ejecución rechazó `release 21`. Se descargó un JDK Temurin **21** en `/tmp/sigsu-jdk` y se ejecutaron las pruebas con `JAVA_HOME` apuntándolo. El proyecto no depende de esa ruta. El Dockerfile usa JDK 21 y compila correctamente.
- Se formateó Java con google-java-format 1.24.0, estilo AOSP. No se agrega como dependencia del proyecto.

## Persistencia y concurrencia

- Entidades JPA sencillas con constructor explícito sin argumentos y atributos públicos. Los controladores responden DTOs record y nunca exponen `Usuario.contrasena`. No hay Lombok.
- UUID para las claves de historial, permitido por la especificación. Todos los tiempos persistidos son `Instant`/timestamptz; días inhábiles son `LocalDate`/date. Formularios usan `@IdClass` con clave compuesta y JSONB. Solicitud solo guarda UUIDs de servicio y usuarios.
- La secuencia de folios es global, no se reinicia al cambiar de año. El año se toma en America/Chihuahua. PostgreSQL puede consumir números de secuencia en transacciones revertidas: los folios son únicos, no consecutivos sin huecos. `%06d` expresa ancho mínimo; después de 999999 crece para conservar unicidad.
- Registro usa `TransactionTemplate`: condiciones, validación, `saveAndFlush`, historial y evento están en la misma transacción. Al salir con excepción ya se ejecutó rollback; solo entonces se busca el folio abierto. Si la solicitud concurrente se cerró justo antes de buscarla, responde conflicto de duplicado sin folio, en lugar de inventarlo.
- Toma usa el UPDATE nativo solicitado, con versión y estados permitidos. No modifica la entidad administrada antes del UPDATE: un flush previo podría alterar indebidamente el resultado. `clearAutomatically` descarta la entidad tras la sentencia y el objeto separado se actualiza para construir DTO e historial.
- Cierre y cancelación usan `@Version` y `saveAndFlush`. Una colisión optimista se convierte en 409. La cancelación no necesita una versión en el body: se controla la versión leída dentro de la transacción.
- Vencimientos usa JPA sobre filas adquiridas mediante `FOR UPDATE SKIP LOCKED`, alternativa autorizada a las dos sentencias masivas. Cada fila incrementa versión, agrega historial y publica evento dentro de la misma transacción. Dos ejecuciones concurrentes no procesan la misma fila. El límite es estrictamente `vence_en < ahora`; al instante exacto aún no vence.
- Publicar formularios concurrentemente puede producir conflicto de clave compuesta; se responde 409 y el administrador debe reintentar. La versión nunca se sobrescribe. Crear servicio y formulario v1 comparte transacción y se apoya en la FK diferida.
- `RegistrarCambio` centraliza exclusivamente escritura de historial y publicación de `CambioDeSolicitud`. Solicitudes obtiene catálogo solo por `ICatalogo`/DTOs y usuarios por `IUsuarios`; no importa entidades ni repositorios de esos módulos. Notificaciones solo recibe el evento de `compartido`, con correos ya resueltos.

## Reglas mínimas y semilla

- El restablecimiento de contraseña acepta objeto vacío; `correoAlterno` es opcional y exige formato email si viene. Préstamo de laboratorio acepta Redes, Software, Electrónica o Manufactura. `horario` comprueba HH:MM-HH:MM, sin validar orden cronológico; `fecha` comprueba formato date, sin exigir fecha futura. No se agregan reglas de producto no solicitadas.
- Se incluyen cuatro días inhábiles: 2 y 16 de noviembre de 2026, 25 de diciembre de 2026 y 1 de enero de 2027. No se agregan vacaciones opcionales.
- El catálogo lista solo servicios activos. El detalle puede mostrar uno inactivo para consultar solicitudes históricas; registrar contra él devuelve 422. La bandeja contiene únicamente los cuatro estados abiertos, incluso si se pide un estado cerrado.
- Un usuario multirrol puede actuar como estudiante dueño o responsable de área; no se prohíbe que tome su propia solicitud si tiene ambos permisos. No hay restricción de segregación de funciones en el modelo.
- Crear usuario exige contraseña de al menos ocho caracteres, correo válido y conjunto no vacío de roles conocidos. Asignar área requiere rol RESPONSABLE. Un área inexistente se rechaza por FK; no se agrega dependencia de Seguridad a las entidades de Catálogo.
- El endpoint de días inhábiles permite reemplazar el motivo cuando la fecha ya existe. Los parámetros se listan y guardan como clave/valor. La zona horaria del cálculo está fijada en America/Chihuahua por especificación; no cambia en caliente por editar `zona.horaria`. El remitente real se toma de `sigsu.correo.remitente` (env `SIGSU_CORREO_REMITENTE`) al arrancar. Los parámetros de ejemplo no se usan como configuración dinámica de infraestructura, lo que evita acoplar Notificaciones a Administración.

## Idempotencia, adjuntos y correo

- El filtro se instancia exclusivamente dentro de `SecurityFilterChain`, después de `AuthorizationFilter`. No es `@Component` ni bean de filtro servlet; no hay doble ejecución. La llave usa el correo autenticado, nunca un correo enviado en el cuerpo.
- TTL 24 h, ajustable por configuración. Además del método, ruta y cuerpo, la huella incluye query string porque el parámetro `ahora` cambia el resultado de un POST. Los bytes de respuesta se serializan en Base64 dentro del JSON de Redis, preservando el cuerpo exacto. La reproducción conserva status y Content-Type; solo agrega `Idempotent-Replayed`.
- Una llave debe medir entre 1 y 200 caracteres. Al vencer la llave ya no se garantiza reproducción; los cambios de negocio siguen protegidos por PostgreSQL. Redis caído produce error de servidor, en vez de ejecutar silenciosamente una petición que pidió idempotencia. No hay persistencia de Redis entre eliminación/recreación de su contenedor.
- Se lee un máximo de 6 MB por petición con llave; tamaño mayor devuelve 413. Multipart limita cada archivo a 5 MB y el total a 6 MB. Como el filtro ya leyó el flujo original, `PeticionRepetible` usa el analizador multipart del Tomcat embebido sobre la copia y expone `Part` al resolver MVC. Evita implementar un parser MIME propio o agregar otra dependencia; esta pequeña clase está ligada a Tomcat y debe revisarse si se cambia de contenedor. Tiene prueba unitaria y demostración real de reproducción de multipart idéntico.
- La huella compara bytes, no equivalencia semántica de JSON. Cambiar espacios, query string o frontera MIME cambia la petición. El script conserva exactamente el multipart original para el reintento.
- Los adjuntos se almacenan con nombre UUID bajo la raíz configurada; la ruta física no se expone en DTO. La descarga usa `application/octet-stream` y Content-Disposition attachment; `tipo` conserva el MIME informado en el metadato. No se implementa antivirus.
- Correo usa evento `AFTER_COMMIT` + `@Async`, sin reintentos persistentes. Un fallo de SMTP solo genera `log.warn`; no revierte una solicitud confirmada. Si el proceso se cae entre commit y envío se puede perder el aviso, límite aceptado del diseño sin outbox.

## Entrega y límites

No se omitió ningún endpoint de la administración mínima solicitada. La fase de pruebas añade una colección Postman repetible, reportes Newman, evidencias directas y TRAZABILIDAD.md. No hay interfaz HTML, despliegue remoto, pagos, firma electrónica ni integración escolar; CAMBIOS.md queda fuera de esta tarea. La gestión completa de áreas/categorías, modificación de roles de un usuario existente y desactivación de usuarios no tiene endpoints en esta fase; la semilla y la creación multirrol cubren el alcance mínimo. No hay borrado de solicitudes ni historial.

Las verificaciones reales están en `docs/VERIFICACION.md` y se pueden repetir con `scripts/verificar.py`. El script crea cuentas y solicitudes propias para no necesitar borrar ni reinicializar la BD. Los contenedores se dejan levantados. El repositorio es local, sin remoto ni push y sin atribuciones en commits.
