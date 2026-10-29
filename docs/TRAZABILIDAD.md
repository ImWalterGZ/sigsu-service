# Trazabilidad del diseño a la implementación y sus pruebas

Las cinco tablas cruzan todos los elementos escritos en los diagramas de la Evidencia 3 con el prototipo ejecutado. Las fuentes son `clases.py`, `paquetes.py`, `componentes.py`, `secuencia.py` y `despliegue.py` en `/home/gzzz/.t3/scratch/2026-10-27-contexto-walter-estudiante-de-ingenier-f53dc9c2/out/uml/`, junto con `../contenido.md`. No se confunden los nombres de capas del diseño con los paquetes finales.

Los nombres de requests son literales de `postman/SIGSU.postman_collection.json`. Las filas de clases, atributos y relaciones señalan requests que ejercitan indirectamente su persistencia; los campos internos no expuestos (por ejemplo ruta física) se comprueban en código. La evidencia Newman prueba el comportamiento HTTP del recorrido; la existencia de un atributo, una FK o una dependencia interna se comprueba en el archivo indicado y en `estructura.txt`, no se deduce solo del status HTTP. «Sin request específico» identifica una comprobación estática o de infraestructura. Los archivos de la última columna son relativos a `docs/evidencias/`.

Las dos ejecuciones de Newman pasan sin reiniciar entre ellas. El historial representa «sistema» con `autorId: null`; la evidencia SQL lo muestra como texto con `coalesce`. La prueba del cron habilita temporalmente el log SQL y espera la transición real; no llama al endpoint administrativo. Ninguna prueba de volumen demuestra recuperación ante pérdida del host, y no se hizo un benchmark de las 66 solicitudes/hora estimadas en el diseño.

Las sustituciones principales son HTTP Basic en lugar de sesión y páginas; `controller/service/model/repository` en lugar de `web/aplicacion/dominio/infraestructura`; adjuntos posteriores al registro; triggers en lugar de permisos INSERT-only; scheduler dentro de la app; y PostgreSQL, volumen y Mailpit locales en lugar de Render, Persistent Disk y Microsoft 365. No se implementaron LoginController, FiltroDeSesion, ErroresDeValidacion ni SolicitudDuplicada como clases: sus comportamientos se resuelven con Spring Security, BusinessException y los DTO de error. La administración no ofrece CRUD de áreas o categorías: se cargan en la semilla.

## Clases

| Elemento del modelo | Implementación (archivo:línea, relativo al repo) | Request de Postman que lo prueba (nombre exacto) | Evidencia (archivo en docs/evidencias) |
|---|---|---|---|
| Rol | src/main/java/mx/utch/sigsu/seguridad/model/Rol.java:3 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Rol.ESTUDIANTE | src/main/java/mx/utch/sigsu/seguridad/model/Rol.java:4 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Rol.RESPONSABLE | src/main/java/mx/utch/sigsu/seguridad/model/Rol.java:5 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Rol.ADMINISTRADOR | src/main/java/mx/utch/sigsu/seguridad/model/Rol.java:6 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Usuario | src/main/java/mx/utch/sigsu/seguridad/model/Usuario.java:10 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Usuario.id: UUID | src/main/java/mx/utch/sigsu/seguridad/model/Usuario.java:11 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Usuario.correo: String {único} | src/main/java/mx/utch/sigsu/seguridad/model/Usuario.java:12 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Usuario.nombre: String | src/main/java/mx/utch/sigsu/seguridad/model/Usuario.java:13 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Usuario.roles: Set<Rol> | src/main/java/mx/utch/sigsu/seguridad/model/Usuario.java:21 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Usuario.activo: boolean | src/main/java/mx/utch/sigsu/seguridad/model/Usuario.java:15 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| ResponsableArea | src/main/java/mx/utch/sigsu/seguridad/model/ResponsableArea.java:10 | R8 · Asignar responsable a un área | newman-resultado.json; newman-segunda-resultado.json |
| ResponsableArea.asignadaEn: Date | src/main/java/mx/utch/sigsu/seguridad/model/ResponsableArea.java:14 | R8 · Asignar responsable a un área | newman-resultado.json; newman-segunda-resultado.json |
| Area | src/main/java/mx/utch/sigsu/catalogo/model/Area.java:10 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Area.id: UUID | src/main/java/mx/utch/sigsu/catalogo/model/Area.java:11 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Area.nombre: String | src/main/java/mx/utch/sigsu/catalogo/model/Area.java:12 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Area.correo: String | src/main/java/mx/utch/sigsu/catalogo/model/Area.java:13 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Categoria | src/main/java/mx/utch/sigsu/catalogo/model/Categoria.java:10 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Categoria.id: UUID | src/main/java/mx/utch/sigsu/catalogo/model/Categoria.java:11 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Categoria.nombre: String | src/main/java/mx/utch/sigsu/catalogo/model/Categoria.java:12 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:10 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio.id: UUID | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:11 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio.nombre: String | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:12 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio.descripcion: String | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:15 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio.plazoDiasHabiles: int | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:19 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio.versionVigente: int | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:20 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio.activo: boolean | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:21 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio.formularioVigente(): FormularioServicio | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:27 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Servicio.calcularVencimiento(desde): Instant | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:31 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| FormularioServicio | src/main/java/mx/utch/sigsu/catalogo/model/FormularioServicio.java:14 | R8 · Publicar versión 2 del formulario | newman-resultado.json; newman-segunda-resultado.json |
| FormularioServicio.version: int | src/main/java/mx/utch/sigsu/catalogo/model/FormularioServicio.java:16 | R8 · Publicar versión 2 del formulario | newman-resultado.json; newman-segunda-resultado.json |
| FormularioServicio.esquema: JSONB  (JSON Schema) | src/main/java/mx/utch/sigsu/catalogo/model/FormularioServicio.java:19 | R8 · Publicar versión 2 del formulario | newman-resultado.json; newman-segunda-resultado.json |
| FormularioServicio.creadoEn: Instant | src/main/java/mx/utch/sigsu/catalogo/model/FormularioServicio.java:21 | R8 · Publicar versión 2 del formulario | newman-resultado.json; newman-segunda-resultado.json |
| FormularioServicio.validar(datos): Errores | src/main/java/mx/utch/sigsu/catalogo/model/FormularioServicio.java:25 | R1 · Falta motivo → 422 con campos | newman-resultado.json; newman-segunda-resultado.json |
| DiaInhabil | src/main/java/mx/utch/sigsu/catalogo/model/DiaInhabil.java:10 | R8 · Agregar día inhábil | newman-resultado.json; newman-segunda-resultado.json |
| DiaInhabil.fecha: Date | src/main/java/mx/utch/sigsu/catalogo/model/DiaInhabil.java:11 | R8 · Agregar día inhábil | newman-resultado.json; newman-segunda-resultado.json |
| DiaInhabil.motivo: String | src/main/java/mx/utch/sigsu/catalogo/model/DiaInhabil.java:12 | R8 · Agregar día inhábil | newman-resultado.json; newman-segunda-resultado.json |
| Estado | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:5 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Estado.REGISTRADA | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:6 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Estado.SIN_RESPUESTA | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:7 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Estado.EN_ATENCION | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:8 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Estado.DEMORADO | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:9 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Estado.ATENDIDA | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:10 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Estado.RECHAZADA | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:11 | Sin request específico: comprobación estática | pruebas-unitarias.txt |
| Estado.CANCELADA | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:12 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:13 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.id: UUID | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:14 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.folio: String {único} | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:15 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.datos: JSONB | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:21 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.estado: Estado | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:24 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.venceEn: Instant | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:27 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.creadaEn: Instant | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:28 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.cerradaEn: Instant [0..1] | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:29 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.version: int | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:30 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.tomar(responsable) | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:34 | R3 · Responsable A toma con versión leída → 200 | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.cambiarEstado(nuevo, observacion) | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:41 | R4 · Atender con observación → 200 y cerradaEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.cancelar() | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:52 | R4 · Dueño cancela registrada → 200 CANCELADA | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud.vencer(ahora) | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:59 | R5 · Consultar SIN_RESPUESTA e historial del sistema | newman-resultado.json; newman-segunda-resultado.json |
| Historial | src/main/java/mx/utch/sigsu/solicitudes/model/Historial.java:10 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Historial.anterior: Estado [0..1] | src/main/java/mx/utch/sigsu/solicitudes/model/Historial.java:16 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Historial.nuevo: Estado | src/main/java/mx/utch/sigsu/solicitudes/model/Historial.java:19 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Historial.observacion: String | src/main/java/mx/utch/sigsu/solicitudes/model/Historial.java:22 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Historial.fecha: Instant | src/main/java/mx/utch/sigsu/solicitudes/model/Historial.java:24 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Adjunto | src/main/java/mx/utch/sigsu/solicitudes/model/Adjunto.java:10 | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| Adjunto.nombre: String | src/main/java/mx/utch/sigsu/solicitudes/model/Adjunto.java:13 | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| Adjunto.ruta: String | src/main/java/mx/utch/sigsu/solicitudes/model/Adjunto.java:14 | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| Adjunto.tipo: String | src/main/java/mx/utch/sigsu/solicitudes/model/Adjunto.java:15 | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| Adjunto.bytes: long | src/main/java/mx/utch/sigsu/solicitudes/model/Adjunto.java:16 | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| Parametro | src/main/java/mx/utch/sigsu/administracion/model/Parametro.java:10 | R8 · Guardar parámetro administrativo | newman-resultado.json; newman-segunda-resultado.json |
| Parametro.clave: String | src/main/java/mx/utch/sigsu/administracion/model/Parametro.java:11 | R8 · Guardar parámetro administrativo | newman-resultado.json; newman-segunda-resultado.json |
| Parametro.valor: String | src/main/java/mx/utch/sigsu/administracion/model/Parametro.java:12 | R8 · Guardar parámetro administrativo | newman-resultado.json; newman-segunda-resultado.json |
| Usuario.correo {único} | src/main/resources/db/migration/V1__esquema_inicial.sql:5 | R8 · Crear usuario con dos roles | estructura.txt |
| ResponsableArea {único(usuario, área)} | src/main/resources/db/migration/V1__esquema_inicial.sql:31 | R8 · Asignar responsable a un área | estructura.txt |
| FormularioServicio {clave(servicio, versión)} | src/main/resources/db/migration/V1__esquema_inicial.sql:49 | R8 · Publicar versión 2 del formulario | estructura.txt |
| FormularioServicio {inmutable / solo inserción} | src/main/resources/db/migration/V1__esquema_inicial.sql:108 | R8 · Publicar versión 2 del formulario | historial-inmutable.txt |
| Historial {solo inserción}, UPDATE/DELETE/TRUNCATE | src/main/resources/db/migration/V1__esquema_inicial.sql:105 | R7 · Detalle con historial ordenado | historial-inmutable.txt |
| Historial: rechazo de TRUNCATE | src/main/resources/db/migration/V1__esquema_inicial.sql:106 | R7 · Detalle con historial ordenado | historial-inmutable.txt |
| Solicitud.folio {único} | src/main/resources/db/migration/V1__esquema_inicial.sql:63 | R1 · Registrar constancia → 201 con folio y venceEn | estructura.txt |
| Solicitud abierta única por estudiante y servicio (índice parcial) | src/main/resources/db/migration/V1__esquema_inicial.sql:78 | R2 · Duplicado abierto → 409 con folio existente | indice-parcial.txt |
| Usuario 0..* ↔ 0..* Area mediante ResponsableArea | src/main/resources/db/migration/V1__esquema_inicial.sql:26 | R8 · Asignar responsable a un área | estructura.txt |
| ResponsableArea → Usuario | src/main/resources/db/migration/V1__esquema_inicial.sql:28 | R8 · Asignar responsable a un área | estructura.txt |
| ResponsableArea → Area | src/main/resources/db/migration/V1__esquema_inicial.sql:29 | R8 · Asignar responsable a un área | estructura.txt |
| Servicio → Area | src/main/resources/db/migration/V1__esquema_inicial.sql:34 | R0 · Consultar constancia con esquema | estructura.txt |
| Servicio → Categoria | src/main/resources/db/migration/V1__esquema_inicial.sql:39 | R0 · Consultar constancia con esquema | estructura.txt |
| Servicio ◆ FormularioServicio (versiones) | src/main/resources/db/migration/V1__esquema_inicial.sql:45 | R8 · Publicar versión 2 del formulario | estructura.txt |
| Servicio → formulario vigente: FK diferida | src/main/resources/db/migration/V1__esquema_inicial.sql:52 | R8 · Publicar versión 2 del formulario | esquema-despues.sql |
| Solicitud → Usuario estudiante | src/main/resources/db/migration/V1__esquema_inicial.sql:64 | R1 · Registrar constancia → 201 con folio y venceEn | estructura.txt |
| Solicitud → Usuario responsable opcional | src/main/resources/db/migration/V1__esquema_inicial.sql:69 | R3 · Responsable A toma con versión leída → 200 | estructura.txt |
| Historial → Usuario autor opcional (null = sistema) | src/main/resources/db/migration/V1__esquema_inicial.sql:82 | R5 · Consultar SIN_RESPUESTA e historial del sistema | vencimiento-programado.txt |
| Historial → Solicitud (referencia independiente) | src/main/resources/db/migration/V1__esquema_inicial.sql:79 | R7 · Detalle con historial ordenado | estructura.txt |
| Solicitud ◆ Adjunto | src/main/resources/db/migration/V1__esquema_inicial.sql:88 | R10 · Subir identificación pequeña → 201 | estructura.txt |
| Solicitud → FormularioServicio: FK compuesta fija la versión | src/main/resources/db/migration/V1__esquema_inicial.sql:74 | R8 · Publicar versión 2 del formulario | estructura.txt |
| Servicio usa DiaInhabil | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:38 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Usuario usa Set<Rol> (sin herencia por rol) | src/main/java/mx/utch/sigsu/seguridad/model/Usuario.java:21 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| Solicitud usa Estado | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:24 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| tomar exige la misma versión leída: UPDATE condicional | src/main/java/mx/utch/sigsu/solicitudes/repository/SolicitudRepositorio.java:25 | R3 · Responsable A toma con versión leída → 200 | toma-concurrente.txt |

## Paquetes

| Elemento del modelo | Implementación (archivo:línea, relativo al repo) | Request de Postman que lo prueba (nombre exacto) | Evidencia (archivo en docs/evidencias) |
|---|---|---|---|
| seguridad.web.LoginController | src/main/java/mx/utch/sigsu/seguridad/service/ConfiguracionSeguridad.java:57 — no implementado / sustituido por HTTP Basic stateless en ConfiguracionSeguridad y Autenticar | R7 · Sin credenciales → 401 | newman-resultado.json; newman-segunda-resultado.json |
| seguridad.web.FiltroDeSesion | src/main/java/mx/utch/sigsu/seguridad/service/ConfiguracionSeguridad.java:57 — no implementado / sustituido por HTTP Basic stateless en ConfiguracionSeguridad y Autenticar | R7 · Sin credenciales → 401 | newman-resultado.json; newman-segunda-resultado.json |
| seguridad.aplicacion.IUsuarios | src/main/java/mx/utch/sigsu/seguridad/service/IUsuarios.java:1 — capa aplicacion renombrada a service | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| seguridad.aplicacion.Autenticar | src/main/java/mx/utch/sigsu/seguridad/service/Autenticar.java:1 — capa aplicacion renombrada a service | R7 · Sin credenciales → 401 | newman-resultado.json; newman-segunda-resultado.json |
| seguridad.dominio.Usuario | src/main/java/mx/utch/sigsu/seguridad/model/Usuario.java:1 — capa dominio renombrada a model | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| seguridad.dominio.Rol | src/main/java/mx/utch/sigsu/seguridad/model/Rol.java:1 — capa dominio renombrada a model | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| seguridad.dominio.ResponsableArea | src/main/java/mx/utch/sigsu/seguridad/model/ResponsableArea.java:1 — capa dominio renombrada a model | R8 · Asignar responsable a un área | newman-resultado.json; newman-segunda-resultado.json |
| seguridad.infraestructura.UsuarioRepositorio | src/main/java/mx/utch/sigsu/seguridad/repository/UsuarioRepositorio.java:1 — capa infraestructura renombrada a repository | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| administracion.web.AdminController | src/main/java/mx/utch/sigsu/administracion/controller/AdminController.java:1 — capa web renombrada a controller | R8 · Guardar parámetro administrativo | newman-resultado.json; newman-segunda-resultado.json |
| administracion.aplicacion.GestionarUsuarios | src/main/java/mx/utch/sigsu/administracion/service/GestionarUsuarios.java:1 — capa aplicacion renombrada a service | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| administracion.aplicacion.GestionarParametros | src/main/java/mx/utch/sigsu/administracion/service/GestionarParametros.java:1 — capa aplicacion renombrada a service | R8 · Guardar parámetro administrativo | newman-resultado.json; newman-segunda-resultado.json |
| administracion.dominio.Parametro | src/main/java/mx/utch/sigsu/administracion/model/Parametro.java:1 — capa dominio renombrada a model | R8 · Guardar parámetro administrativo | newman-resultado.json; newman-segunda-resultado.json |
| administracion.infraestructura.ParametroRepositorio | src/main/java/mx/utch/sigsu/administracion/repository/ParametroRepositorio.java:1 — capa infraestructura renombrada a repository | R8 · Guardar parámetro administrativo | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.web.CatalogoController | src/main/java/mx/utch/sigsu/catalogo/controller/CatalogoController.java:1 — capa web renombrada a controller | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.aplicacion.ICatalogo | src/main/java/mx/utch/sigsu/catalogo/service/ICatalogo.java:1 — capa aplicacion renombrada a service | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.aplicacion.IGestionCatalogo | src/main/java/mx/utch/sigsu/catalogo/service/IGestionCatalogo.java:1 — capa aplicacion renombrada a service | R8 · Publicar versión 2 del formulario | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.dominio.Area | src/main/java/mx/utch/sigsu/catalogo/model/Area.java:1 — capa dominio renombrada a model | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.dominio.Categoria | src/main/java/mx/utch/sigsu/catalogo/model/Categoria.java:1 — capa dominio renombrada a model | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.dominio.Servicio | src/main/java/mx/utch/sigsu/catalogo/model/Servicio.java:1 — capa dominio renombrada a model | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.dominio.FormularioServicio | src/main/java/mx/utch/sigsu/catalogo/model/FormularioServicio.java:1 — capa dominio renombrada a model | R8 · Publicar versión 2 del formulario | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.dominio.DiaInhabil | src/main/java/mx/utch/sigsu/catalogo/model/DiaInhabil.java:1 — capa dominio renombrada a model | R8 · Agregar día inhábil | newman-resultado.json; newman-segunda-resultado.json |
| catalogo.infraestructura.ServicioRepositorio | src/main/java/mx/utch/sigsu/catalogo/repository/ServicioRepositorio.java:1 — capa infraestructura renombrada a repository | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.web.SolicitudController | src/main/java/mx/utch/sigsu/solicitudes/controller/SolicitudController.java:1 — capa web renombrada a controller | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.web.BandejaController | src/main/java/mx/utch/sigsu/solicitudes/controller/BandejaController.java:1 — capa web renombrada a controller | R7 · Bandeja ordenada por venceEn ascendente | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.aplicacion.RegistrarSolicitud | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:1 — capa aplicacion renombrada a service | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.aplicacion.TomarSolicitud | src/main/java/mx/utch/sigsu/solicitudes/service/TomarSolicitud.java:1 — capa aplicacion renombrada a service | R3 · Responsable A toma con versión leída → 200 | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.aplicacion.CambiarEstado | src/main/java/mx/utch/sigsu/solicitudes/service/CambiarEstado.java:1 — capa aplicacion renombrada a service | R4 · Atender con observación → 200 y cerradaEn | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.aplicacion.VencerSolicitudes | src/main/java/mx/utch/sigsu/solicitudes/service/VencerSolicitudes.java:1 — capa aplicacion renombrada a service | R5 · Consultar SIN_RESPUESTA e historial del sistema | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.dominio.Solicitud | src/main/java/mx/utch/sigsu/solicitudes/model/Solicitud.java:1 — capa dominio renombrada a model | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.dominio.Historial | src/main/java/mx/utch/sigsu/solicitudes/model/Historial.java:1 — capa dominio renombrada a model | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.dominio.Adjunto | src/main/java/mx/utch/sigsu/solicitudes/model/Adjunto.java:1 — capa dominio renombrada a model | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.dominio.Estado | src/main/java/mx/utch/sigsu/solicitudes/model/Estado.java:1 — capa dominio renombrada a model | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.infraestructura.SolicitudRepositorio | src/main/java/mx/utch/sigsu/solicitudes/repository/SolicitudRepositorio.java:1 — capa infraestructura renombrada a repository | R7 · Bandeja ordenada por venceEn ascendente | newman-resultado.json; newman-segunda-resultado.json |
| solicitudes.infraestructura.AlmacenDeAdjuntos | src/main/java/mx/utch/sigsu/solicitudes/repository/AlmacenDeAdjuntos.java:1 — capa infraestructura renombrada a repository | R10 · Descargar identificación → 200 | newman-resultado.json; newman-segunda-resultado.json |
| notificaciones.aplicacion.AvisarCambio | src/main/java/mx/utch/sigsu/notificaciones/service/AvisarCambio.java:1 — capa aplicacion renombrada a service | R6 · Mailpit contiene correo del folio | newman-resultado.json; newman-segunda-resultado.json |
| notificaciones.dominio.Aviso | src/main/java/mx/utch/sigsu/notificaciones/model/Aviso.java:1 — capa dominio renombrada a model | R6 · Mailpit contiene correo del folio | newman-resultado.json; newman-segunda-resultado.json |
| notificaciones.infraestructura.CorreoSmtp | src/main/java/mx/utch/sigsu/notificaciones/repository/CorreoSmtp.java:1 — capa infraestructura renombrada a repository | R6 · Mailpit contiene correo del folio | newman-resultado.json; newman-segunda-resultado.json |
| compartido.UsuarioActual | src/main/java/mx/utch/sigsu/compartido/UsuarioActual.java:1 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| compartido.CambioDeSolicitud | src/main/java/mx/utch/sigsu/compartido/CambioDeSolicitud.java:1 | R6 · Mailpit contiene correo del folio | newman-resultado.json; newman-segunda-resultado.json |
| compartido.Folio | src/main/java/mx/utch/sigsu/compartido/Folio.java:1 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| administracion → seguridad.IUsuarios | src/main/java/mx/utch/sigsu/administracion/service/GestionarUsuarios.java:4 | R8 · Crear usuario con dos roles | estructura.txt |
| administracion → catalogo.IGestionCatalogo | src/main/java/mx/utch/sigsu/administracion/controller/AdminController.java:8 | R8 · Publicar versión 2 del formulario | estructura.txt |
| solicitudes → catalogo.ICatalogo | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:3 | R1 · Registrar constancia → 201 con folio y venceEn | estructura.txt |
| solicitudes → seguridad.IUsuarios | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarCambio.java:5 | R6 · Mailpit contiene correo del folio | estructura.txt |
| Capas controller → service → model/repository | src/main/java/mx/utch/sigsu/solicitudes/controller/SolicitudController.java:7 | R1 · Registrar constancia → 201 con folio y venceEn | estructura.txt |
| Notificaciones sin pantallas | src/main/java/mx/utch/sigsu/notificaciones/service/AvisarCambio.java:13 | R6 · Mailpit contiene correo del folio | estructura.txt |

## Componentes

| Elemento del modelo | Implementación (archivo:línea, relativo al repo) | Request de Postman que lo prueba (nombre exacto) | Evidencia (archivo en docs/evidencias) |
|---|---|---|---|
| Seguridad: rol previo al módulo | src/main/java/mx/utch/sigsu/seguridad/service/ConfiguracionSeguridad.java:74 | R7 · Bandeja ordenada por venceEn ascendente | newman-resultado.json; newman-segunda-resultado.json |
| Administración: usuarios, roles y parámetros | src/main/java/mx/utch/sigsu/administracion/controller/AdminController.java:18 | R8 · Guardar parámetro administrativo | newman-resultado.json; newman-segunda-resultado.json |
| Catálogo: servicios y formularios | src/main/java/mx/utch/sigsu/catalogo/service/Catalogo.java:17 | R0 · Consultar constancia con esquema | newman-resultado.json; newman-segunda-resultado.json |
| Solicitudes: registro | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:17 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Solicitudes: bandeja | src/main/java/mx/utch/sigsu/solicitudes/controller/BandejaController.java:15 | R7 · Bandeja ordenada por venceEn ascendente | newman-resultado.json; newman-segunda-resultado.json |
| Solicitudes: toma | src/main/java/mx/utch/sigsu/solicitudes/service/TomarSolicitud.java:15 | R3 · Responsable A toma con versión leída → 200 | newman-resultado.json; newman-segunda-resultado.json |
| Solicitudes: historial | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarCambio.java:17 | R7 · Detalle con historial ordenado | newman-resultado.json; newman-segunda-resultado.json |
| Solicitudes: adjuntos | src/main/java/mx/utch/sigsu/solicitudes/service/GestionarAdjuntos.java:18 | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| Solicitudes: vencimientos | src/main/java/mx/utch/sigsu/solicitudes/service/VencerSolicitudes.java:16 | R5 · Consultar SIN_RESPUESTA e historial del sistema | newman-resultado.json; newman-segunda-resultado.json |
| Notificaciones después del COMMIT | src/main/java/mx/utch/sigsu/notificaciones/service/AvisarCambio.java:22 | R6 · Mailpit contiene correo del folio | newman-resultado.json; newman-segunda-resultado.json |
| IUsuarios | src/main/java/mx/utch/sigsu/seguridad/service/IUsuarios.java:7 | R8 · Crear usuario con dos roles | newman-resultado.json; newman-segunda-resultado.json |
| IGestionCatalogo | src/main/java/mx/utch/sigsu/catalogo/service/IGestionCatalogo.java:7 | R8 · Publicar versión 2 del formulario | newman-resultado.json; newman-segunda-resultado.json |
| ICatalogo: esquema, plazo y días inhábiles | src/main/java/mx/utch/sigsu/catalogo/service/ICatalogo.java:9 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| UsuarioActual transporta identidad y roles | src/main/java/mx/utch/sigsu/compartido/UsuarioActual.java:7 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| CambioDeSolicitud: evento interno compartido | src/main/java/mx/utch/sigsu/compartido/CambioDeSolicitud.java:5 | R6 · Mailpit contiene correo del folio | newman-resultado.json; newman-segunda-resultado.json |
| Seguridad: sesión en cookie HttpOnly | src/main/java/mx/utch/sigsu/seguridad/service/ConfiguracionSeguridad.java:57 — no implementado / sustituido por HTTP Basic sin sesión | R7 · Sin credenciales → 401 | newman-resultado.json; newman-segunda-resultado.json |
| sigsu.jar: monolito Spring Boot | Dockerfile:12 | Sin request específico: comprobación estática | compose-ps.txt |
| Acceso JDBC a PostgreSQL | compose.yaml:34 | R1 · Registrar constancia → 201 con folio y venceEn | compose-ps.txt |
| Cada módulo usa tablas propias / interfaces públicas | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:3 — comprobación estática; no equivale a aislamiento por permisos SQL | Sin request específico: comprobación estática | estructura.txt |
| Puerto HTTPS | compose.yaml:41 — no implementado / sustituido por HTTP local 8080 sin terminación TLS | Sin request específico: comprobación estática | compose-ps.txt |
| SMTP Microsoft 365 | compose.yaml:38 — no implementado / sustituido por Mailpit local | R6 · Mailpit contiene correo del folio | mailpit.txt |
| Render Postgres | compose.yaml:3 — no implementado / sustituido por PostgreSQL 16 local en Compose | Sin request específico: comprobación estática | compose-ps.txt |
| Persistent Disk | compose.yaml:42 — no implementado / sustituido por volumen nombrado datos-adjuntos | R10 · Subir identificación pequeña → 201 | compose-ps.txt |
| Render Cron / --vencimientos | src/main/java/mx/utch/sigsu/solicitudes/service/VencerSolicitudes.java:30 — no implementado / sustituido por @Scheduled dentro de la app | R5 · Consultar SIN_RESPUESTA e historial del sistema | vencimiento-programado.txt |
| Seguridad comprueba el área antes de la toma | src/main/java/mx/utch/sigsu/solicitudes/service/TomarSolicitud.java:32 | R3 · Otra área no puede tomar → 403 | newman-resultado.json; newman-segunda-resultado.json |
| Administración completa de áreas y categorías | src/main/resources/db/migration/V2__catalogo_y_usuarios_de_prueba.sql:1 — no implementado / sustituido por semilla; no hay endpoints CRUD de áreas o categorías | Sin request específico: comprobación estática | estructura.txt |

## Secuencia

| Elemento del modelo | Implementación (archivo:línea, relativo al repo) | Request de Postman que lo prueba (nombre exacto) | Evidencia (archivo en docs/evidencias) |
|---|---|---|---|
| Estudiante → seguridad: POST /solicitudes | src/main/java/mx/utch/sigsu/solicitudes/controller/SolicitudController.java:39 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Seguridad → controlador: sesión y rol válidos + UsuarioActual | src/main/java/mx/utch/sigsu/seguridad/service/ConfiguracionSeguridad.java:57 — no implementado / sustituido por HTTP Basic y UsuarioActual, sin sesión | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Controlador → RegistrarSolicitud: registrar(estudiante, servicio, datos, archivos) | src/main/java/mx/utch/sigsu/solicitudes/controller/SolicitudController.java:43 — firma sustituida por JSON; archivos se cargan después | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| RegistrarSolicitud → ICatalogo: condiciones(servicio, ahora) | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:42 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| ICatalogo → RegistrarSolicitud: esquema vigente y venceEn | src/main/java/mx/utch/sigsu/catalogo/service/Catalogo.java:47 — el ejemplo v3 del diagrama es ilustrativo; la semilla usa v1 y la prueba publica v2 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| RegistrarSolicitud valida los datos con el esquema vigente | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:44 | R1 · Falta motivo → 422 con campos | newman-resultado.json; newman-segunda-resultado.json |
| Datos inválidos → ErroresDeValidacion | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:47 — no existe clase ErroresDeValidacion; sustituida por BusinessException y ErrorCampo | R1 · Falta motivo → 422 con campos | newman-resultado.json; newman-segunda-resultado.json |
| Controlador → estudiante: 422 con campos marcados | src/main/java/mx/utch/sigsu/compartido/ManejadorErrores.java:15 | R1 · Falta motivo → 422 con campos | newman-resultado.json; newman-segunda-resultado.json |
| RegistrarSolicitud → AlmacenDeAdjuntos: guardar(archivos) | src/main/java/mx/utch/sigsu/solicitudes/service/GestionarAdjuntos.java:39 — no implementado en registro; sustituido por POST posterior /adjuntos | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| AlmacenDeAdjuntos devuelve rutas | src/main/java/mx/utch/sigsu/solicitudes/repository/AlmacenDeAdjuntos.java:21 — ocurre durante la carga posterior, no durante el registro JSON | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| BEGIN · INSERT solicitud, adjuntos e historial | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:40 — solicitud e historial atómicos; adjuntos en otra transacción | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| PostgreSQL → registro: unique_violation · ROLLBACK | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:62 | R2 · Duplicado abierto → 409 con folio existente | newman-resultado.json; newman-segunda-resultado.json |
| RegistrarSolicitud → AlmacenDeAdjuntos: borrar(rutas) tras duplicado | src/main/java/mx/utch/sigsu/solicitudes/service/GestionarAdjuntos.java:62 — no implementado en duplicados de registro JSON; limpieza solo ante fallo transaccional de carga | R10 · Subir identificación pequeña → 201 | newman-resultado.json; newman-segunda-resultado.json |
| RegistrarSolicitud → controlador: SolicitudDuplicada(folio) | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:68 — no existe clase SolicitudDuplicada; sustituida por BusinessException con folioExistente | R2 · Duplicado abierto → 409 con folio existente | newman-resultado.json; newman-segunda-resultado.json |
| Controlador → estudiante: 409 con folio existente | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:73 | R2 · Duplicado abierto → 409 con folio existente | newman-resultado.json; newman-segunda-resultado.json |
| PostgreSQL → registro: COMMIT | src/main/java/mx/utch/sigsu/solicitudes/service/RegistrarSolicitud.java:60 — TransactionTemplate confirma al retornar sin excepción | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| RegistrarSolicitud → AvisarCambio: CambioDeSolicitud(REGISTRADA) después del COMMIT | src/main/java/mx/utch/sigsu/notificaciones/service/AvisarCambio.java:22 — RegistrarCambio publica dentro de la transacción; el listener se ejecuta después del COMMIT | R6 · Mailpit contiene correo del folio | newman-resultado.json; newman-segunda-resultado.json |
| RegistrarSolicitud → controlador: folio, venceEn | src/main/java/mx/utch/sigsu/solicitudes/dto/SolicitudDto.java:8 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| Controlador → estudiante: 201 con folio y fecha | src/main/java/mx/utch/sigsu/solicitudes/controller/SolicitudController.java:40 | R1 · Registrar constancia → 201 con folio y venceEn | newman-resultado.json; newman-segunda-resultado.json |
| AvisarCambio → correo al estudiante y al área | src/main/java/mx/utch/sigsu/notificaciones/service/AvisarCambio.java:27 | R6 · Mailpit contiene correo del folio | newman-resultado.json; newman-segunda-resultado.json |

## Despliegue

| Elemento del modelo | Implementación (archivo:línea, relativo al repo) | Request de Postman que lo prueba (nombre exacto) | Evidencia (archivo en docs/evidencias) |
|---|---|---|---|
| Computadora o celular / navegador web | postman/SIGSU.local.postman_environment.json:5 — no implementado / sustituido por cliente HTTP Postman/Newman; no hay páginas web de SIGSU | R1 · Registrar constancia → 201 con folio y venceEn | newman-salida.txt |
| Render (cloud) | compose.yaml:1 — no implementado / sustituido por Compose local; no hay despliegue en Render | Sin request específico: comprobación estática | compose-ps.txt |
| Web Service · Docker | compose.yaml:31 — app local en contenedor | Sin request específico: comprobación estática | compose-ps.txt |
| sigsu.jar · Spring Boot · Java 21 | Dockerfile:12 — no implementado / sustituido por artefacto local app.jar | Sin request específico: comprobación estática | compose-ps.txt |
| Una instancia / escala vertical | compose.yaml:31 — no implementado / sustituido por una réplica local; escalado vertical no implementado ni medido | Sin request específico: comprobación estática | compose-ps.txt |
| Persistent Disk | compose.yaml:42 — no implementado / sustituido por volumen nombrado datos-adjuntos | R10 · Subir identificación pequeña → 201 | compose-ps.txt |
| Montaje /var/sigsu/adjuntos | compose.yaml:39 — montaje equivalente local | R10 · Descargar identificación → 200 | compose-ps.txt |
| Cron Job · Docker separado | src/main/java/mx/utch/sigsu/solicitudes/service/VencerSolicitudes.java:30 — no implementado / sustituido por @Scheduled en el mismo proceso | R5 · Consultar SIN_RESPUESTA e historial del sistema | vencimiento-programado.txt |
| sigsu.jar --vencimientos / cada 15 minutos | compose.yaml:40 — no implementado / sustituido por cron de seis campos configurado por entorno; sin argumento CLI | R5 · Consultar SIN_RESPUESTA e historial del sistema | vencimiento-programado.txt |
| Render Postgres | compose.yaml:3 — no implementado / sustituido por contenedor PostgreSQL 16 sin gestión Render | Sin request específico: comprobación estática | compose-ps.txt |
| Base sigsu / PostgreSQL 16 | compose.yaml:5 — POSTGRES_DB sigsu | R1 · Registrar constancia → 201 con folio y venceEn | indice-parcial.txt |
| Microsoft 365 / smtp.office365.com | compose.yaml:38 — no implementado / sustituido por Mailpit local | R6 · Mailpit contiene correo del folio | mailpit.txt |
| HTTPS 443 / sigsu.utch.edu.mx | compose.yaml:41 — no implementado / sustituido por HTTP localhost:8080; dominio y TLS no implementados | Sin request específico: comprobación estática | compose-ps.txt |
| JDBC · TLS · red privada (Web Service) | compose.yaml:34 — no implementado / sustituido por JDBC por red Compose; TLS no configurado | R1 · Registrar constancia → 201 con folio y venceEn | compose-ps.txt |
| JDBC · TLS · red privada (Cron Job) | compose.yaml:34 — no implementado / sustituido por mismo DataSource de la app; sin proceso Cron separado ni TLS | R5 · Consultar SIN_RESPUESTA e historial del sistema | vencimiento-programado.txt |
| SMTP 587 · STARTTLS (Web Service y Cron) | src/main/resources/application.yaml:15 — no implementado / sustituido por SMTP Mailpit 1025 sin STARTTLS | R6 · Mailpit contiene correo del folio | mailpit.txt |
| 66 solicitudes por hora / capacidad vertical | compose.yaml:31 — no implementado / sustituido por estimación del diseño, sin benchmark de carga | Sin request específico: comprobación estática | estructura.txt |
| Web Service y Cron usan la misma imagen | src/main/java/mx/utch/sigsu/solicitudes/service/VencerSolicitudes.java:31 — no implementado / sustituido por un solo contenedor ejecuta HTTP y @Scheduled | Sin request específico: comprobación estática | vencimiento-programado.txt |

