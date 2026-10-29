# Evidencias ejecutadas

La colección final se ejecutó primero sobre volúmenes nuevos (`podman compose down -v` y `up -d --build`) y luego otra vez sin reiniciar servicios: **48 requests, 101 assertions, cero fallos en cada corrida**. Se publicaron esquemas distintos (v1 sin campos, v2 exige motivo), verificando que la solicitud anterior conserva v1 y la nueva usa v2. Las **18 pruebas unitarias** pasan con `/tmp/sigsu-jdk` (JDK 21). No fue necesario corregir bugs de la aplicación.

`newman --no-color` no existe en la versión instalada; se utilizó `--color off` y `tee`, con `pipefail`. Los comandos de reproducción están en `../../postman/README.md`. `../../scripts/evidencias.sh` se ejecutó correctamente varias veces; crea un estudiante único por ejecución y no borra datos.

| Archivo | Qué demuestra |
|---|---|
| esquema-antes.sql | `pg_dump --schema-only --no-owner` antes de reformatear V1. |
| esquema-despues.sql | El mismo dump después de reformatear V1 y reconstruir desde volúmenes nuevos. |
| esquema-diff.txt | Archivo vacío: no cambió el esquema. Se excluyeron comentarios de cabecera y delimitadores aleatorios `\restrict`/`\unrestrict` al comparar; no se alteró ninguna definición SQL. |
| newman-resultado.json | Respuestas y assertions de los 48 requests sobre BD limpia. |
| newman-junit.xml | Primera corrida en formato JUnit, cero fallos. |
| newman-salida.txt | Salida CLI sin colores de la primera corrida. |
| newman-segunda-resultado.json | Segunda corrida completa sobre los mismos servicios y datos, sin reiniciar. |
| newman-segunda-junit.xml | Segunda corrida en formato JUnit, cero fallos. |
| newman-segunda-salida.txt | Salida CLI sin colores que acredita repetibilidad. |
| historial-inmutable.txt | Comandos psql y errores esperados de UPDATE, DELETE y TRUNCATE de historial, y UPDATE de formulario. |
| indice-parcial.txt | `\d solicitud` e INSERT duplicado rechazado por `solicitud_abierta`. |
| idempotencia-procesando.txt | Redis con PROCESANDO y POST → 409; dos curl simultáneos con igual llave → 201 y 409 en la ejecución guardada. |
| toma-concurrente.txt | Dos responsables toman la misma versión simultáneamente: uno 200 y otro 409. |
| vencimiento-programado.txt | Cron cada minuto: solicitud con vence_en forzado al pasado → SIN_RESPUESTA, historial sistema y UPDATE/INSERT del log SQL, sin endpoint manual. |
| cron-arranque.txt | Recreación temporal de app con cron cada minuto y log SQL. |
| restauracion-compose.txt | Recreación de app con cron predeterminado y sin el override SQL. |
| mailpit.txt | Asuntos y destinatarios de los correos recibidos. |
| mailpit-respuesta.json | Respuesta completa de Mailpit usada para construir el listado. |
| pruebas-unitarias.txt | Resumen Maven y Surefire: 18 pruebas, 0 fallos, 0 errores. |
| compose-ps.txt | Servicios del proyecto levantados, los cuatro healthy y Actuator UP. |
| estructura.txt | Anclas obtenidas con grep -nF, imports y comprobación de dependencias; referencias y nombres de requests cotejados con TRAZABILIDAD.md. |

El historial usa `autorId: null` para sistema; la consulta SQL lo presenta con `coalesce`. La línea de log del scheduler es SQL de Hibernate habilitado solo durante esa prueba; no se añadió logging ni se modificó código Java. Al terminar, la app conserva `SIGSU_VENCIMIENTOS_CRON=0 */15 * * * *`, sin `SPRING_JPA_SHOW_SQL`.

La comparación de esquema se capturó antes de cualquier cambio a V1. Los reportes finales corresponden a otra reconstrucción limpia con la misma migración ya reformateada. La trazabilidad se encuentra en `../TRAZABILIDAD.md`; distingue verificaciones HTTP, comprobaciones estáticas y elementos sustituidos o sin implementación.
