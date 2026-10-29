#!/usr/bin/env bash
set -euo pipefail
export PODMAN_COMPOSE_WARNING_LOGS=false
cd "$(dirname "$0")/.."
if [[ -z ${COMPOSE:-} ]]; then
  if command -v podman >/dev/null 2>&1; then COMPOSE='podman compose'; else COMPOSE='docker compose'; fi
fi
read -r -a compose <<< "$COMPOSE"
out=docs/evidencias
mkdir -p "$out"
work=$(mktemp -d)
restore=false
cleanup() {
  if $restore; then env -u SIGSU_VENCIMIENTOS_CRON "${compose[@]}" up -d app > "$out/restauracion-compose.txt" 2>&1; fi
  rm -rf "$work"
}
trap cleanup EXIT
base=${SIGSU_URL:-http://localhost:8080}
admin='admin@utch.edu.mx:sigsu123'
resp='resp.escolares@utch.edu.mx:sigsu123'
resp2='resp.escolares2@utch.edu.mx:sigsu123'
psql() { "${compose[@]}" exec -T postgres psql -X -v ON_ERROR_STOP=1 -U sigsu -d sigsu "$@"; }
field() { python3 -c 'import json,sys; print(json.load(sys.stdin)[sys.argv[1]])' "$1"; }
post() { curl -fsS -u "$1" -H 'Content-Type: application/json' -d "$3" "$base$2"; }
uid=$(python3 -c 'import uuid; print(uuid.uuid4())')
correo="evidencia.$uid@alumnos.utch.edu.mx"
post "$admin" /administracion/usuarios "{\"correo\":\"$correo\",\"nombre\":\"Lucía Torres\",\"contrasena\":\"sigsu123\",\"roles\":[\"ESTUDIANTE\"]}" > "$work/usuario.json"
estudiante="$correo:sigsu123"
body='{"servicioId":"00000000-0000-0000-0000-000000000021","datos":{"motivo":"Evidencia de concurrencia","conCalificaciones":true}}'
# El hash concatena método, ruta y bytes exactos igual que FiltroIdempotencia.huella.
huella=$(printf 'POST\n/solicitudes\n%s' "$body" | sha256sum | cut -d' ' -f1)
llave="procesando-$uid"
{
  printf 'redis-cli SET sigsu:idem:%s:%s <RespuestaGuardada PROCESANDO> EX 120\n' "$correo" "$llave"
  "${compose[@]}" exec -T redis redis-cli SET "sigsu:idem:$correo:$llave" "{\"estado\":\"PROCESANDO\",\"huella\":\"$huella\",\"status\":0,\"contentType\":null,\"cuerpo\":null}" EX 120
  printf 'POST /solicitudes Idempotency-Key: %s\n' "$llave"
  status=$(curl -sS -u "$estudiante" -H 'Content-Type: application/json' -H "Idempotency-Key: $llave" -d "$body" -o "$work/procesando.json" -w '%{http_code}' "$base/solicitudes")
  cat "$work/procesando.json"; printf '\nStatus: %s\n' "$status"; [[ $status == 409 ]]
  printf '\nCarrera real: dos curl concurrentes, misma llave y cuerpo.\n'
  for n in 1 2; do
    curl -sS -u "$estudiante" -H 'Content-Type: application/json' -H "Idempotency-Key: carrera-$uid" -d "$body" -D "$work/idem-$n.headers" -o "$work/idem-$n.json" -w '%{http_code}' "$base/solicitudes" > "$work/idem-$n.status" &
  done
  wait
  for n in 1 2; do printf 'curl %s: status %s\n' "$n" "$(cat "$work/idem-$n.status")"; cat "$work/idem-$n.headers" "$work/idem-$n.json"; printf '\n'; done
  python3 - "$work" <<'PY'
import pathlib,sys,json
p=pathlib.Path(sys.argv[1]); codes=[(p/f'idem-{i}.status').read_text() for i in (1,2)]
assert sorted(codes) in [['201','409'],['201','201']],codes
if codes==['201','201']:
 assert any('idempotent-replayed: true' in (p/f'idem-{i}.headers').read_text().lower() for i in (1,2))
for i in (1,2):
 j=json.loads((p/f'idem-{i}.json').read_text())
 if codes[i-1]=='201': (p/'folio').write_text(j['folio'])
print('Carrera válida: una creación; la otra petición obtuvo conflicto en proceso o reproducción.')
PY
} > "$out/idempotencia-procesando.txt" 2>&1
folio=$(cat "$work/folio")
{
  for sql in "UPDATE historial SET observacion='alterado'" 'DELETE FROM historial' 'TRUNCATE historial' 'UPDATE formulario_servicio SET version=version'; do
    printf '\npsql -c "%s"\n' "$sql"
    if psql -c "$sql" > "$work/error" 2>&1; then cat "$work/error"; exit 1; fi
    cat "$work/error"; grep -q 'es de solo inserción' "$work/error"
  done
} > "$out/historial-inmutable.txt" 2>&1
{
  printf 'psql -c "\\d solicitud"\n'; psql -c '\d solicitud'
  sql="INSERT INTO solicitud SELECT gen_random_uuid(), 'DUP-$uid', estudiante_id, servicio_id, formulario_version, datos, estado, responsable_id, vence_en, creada_en, cerrada_en, version FROM solicitud WHERE folio='$folio'"
  printf '\npsql -c "%s"\n' "$sql"
  if psql -c "$sql" > "$work/error" 2>&1; then cat "$work/error"; exit 1; fi
  cat "$work/error"; grep -q 'duplicate key value violates unique constraint "solicitud_abierta"' "$work/error"
} > "$out/indice-parcial.txt" 2>&1
{
  printf 'Dos POST /solicitudes/%s/tomar concurrentes, ambos con version=0.\n' "$folio"
  for n in 1 2; do
    cuenta=$resp; [[ $n == 1 ]] || cuenta=$resp2
    curl -sS -u "$cuenta" -H 'Content-Type: application/json' -d '{"version":0}' -o "$work/toma-$n.json" -w '%{http_code}' "$base/solicitudes/$folio/tomar" > "$work/toma-$n.status" &
  done
  wait
  for n in 1 2; do printf 'Responsable %s: status %s\n' "$n" "$(cat "$work/toma-$n.status")"; cat "$work/toma-$n.json"; printf '\n'; done
  python3 - "$work" <<'PY'
import pathlib,sys
p=pathlib.Path(sys.argv[1]); assert sorted((p/f'toma-{i}.status').read_text() for i in (1,2))==['200','409']
PY
} > "$out/toma-concurrente.txt" 2>&1
# show-sql permite capturar el UPDATE del scheduler sin modificar la aplicación.
cat > "$work/cron.yaml" <<'YAML'
services:
  app:
    environment:
      SPRING_JPA_SHOW_SQL: 'true'
YAML
restore=true
SIGSU_VENCIMIENTOS_CRON='0 * * * * *' "${compose[@]}" -f compose.yaml -f "$work/cron.yaml" up -d app > "$out/cron-arranque.txt" 2>&1
for ((i=0;i<60;i++)); do if curl -fsS "$base/actuator/health" > /dev/null 2>&1; then break; fi; sleep 1; done
{
  printf "Override SIGSU_VENCIMIENTOS_CRON='0 * * * * *'; SQL de Hibernate habilitado solo durante la prueba.\n"
  post "$estudiante" /solicitudes '{"servicioId":"00000000-0000-0000-0000-000000000022","datos":{}}' > "$work/cron.json"
  cronfolio=$(field folio < "$work/cron.json"); cat "$work/cron.json"; printf '\n'
  printf "UPDATE solicitud SET vence_en=now()-interval '1 day' WHERE folio='%s';\n" "$cronfolio"
  psql -c "UPDATE solicitud SET vence_en=now()-interval '1 day' WHERE folio='$cronfolio'"
  inicio=$SECONDS
  for ((i=0;i<70;i++)); do
    estado=$(psql -Atc "SELECT estado FROM solicitud WHERE folio='$cronfolio'")
    if [[ $estado == SIN_RESPUESTA ]]; then break; fi
    sleep 1
  done
  [[ $estado == SIN_RESPUESTA ]]
  printf 'Transición programada observada tras %s segundos; sin llamar /administracion/vencimientos.\n' "$((SECONDS-inicio))"
  psql -c "SELECT s.folio,s.estado,h.anterior,h.nuevo,coalesce(u.nombre,'sistema') AS autor,h.observacion,h.fecha FROM historial h JOIN solicitud s ON s.id=h.solicitud_id LEFT JOIN usuario u ON u.id=h.autor_id WHERE s.folio='$cronfolio' ORDER BY h.fecha"
  printf '\nLíneas del log SQL emitidas por la ejecución programada:\n'
  "${compose[@]}" logs --no-color --since 3m app | grep -E 'Hibernate: (update solicitud|insert into historial)' | tail -n 8
} > "$out/vencimiento-programado.txt" 2>&1
# Restablecer cron y retirar show-sql incluso si la prueba falla (trap).
env -u SIGSU_VENCIMIENTOS_CRON "${compose[@]}" up -d app > "$out/restauracion-compose.txt" 2>&1
restore=false
for ((i=0;i<60;i++)); do if curl -fsS "$base/actuator/health" >/dev/null 2>&1; then break; fi; sleep 1; done
curl -fsS "${SIGSU_MAILPIT_URL:-http://localhost:8025}/api/v1/messages?limit=1000" > "$out/mailpit-respuesta.json"
python3 - "$out" <<'PY'
import json,sys,pathlib
p=pathlib.Path(sys.argv[1]);j=json.loads((p/'mailpit-respuesta.json').read_text())
with (p/'mailpit.txt').open('w') as f:
 f.write(f"GET /api/v1/messages?limit=1000; total: {j['total']}\n")
 for m in j['messages']: f.write(m['Subject']+' | '+', '.join(x['Address'] for x in m['To'])+'\n')
assert j['total']>0
PY
JAVA_HOME=${JAVA_HOME:-/tmp/sigsu-jdk} ./mvnw test > "$work/maven.txt" 2>&1
{ grep -E 'Tests run:|BUILD SUCCESS|Total time:' "$work/maven.txt"; printf '\nInformes Surefire:\n'; cat target/surefire-reports/*.txt; } > "$out/pruebas-unitarias.txt"
# Podman ps expone health aunque algunos proveedores compose omitan ese dato.
healthy=false
for ((i=0;i<60;i++)); do
  salud=$("${compose[@]}" ps --format json)
  if python3 -c 'import json,sys; s=sys.stdin.read(); rows=[json.loads(x) for x in s.splitlines() if x.strip()]; rows=rows[0] if len(rows)==1 and isinstance(rows[0],list) else rows; assert len(rows)==4 and all(x.get("Health")=="healthy" for x in rows)' <<< "$salud" 2>/dev/null 2>&1; then healthy=true; break; fi
  # Verificación alternativa por inspect para Podman y Compose.
  if command -v podman >/dev/null && podman inspect $("${compose[@]}" ps -q) --format '{{.State.Health.Status}}' 2>/dev/null | python3 -c 'import sys; s=sys.stdin.read().split(); assert len(s)==4 and all(x=="healthy" for x in s)'; then healthy=true; break; fi
  sleep 1
done
$healthy
{ "${compose[@]}" ps; if command -v podman >/dev/null 2>&1; then podman ps --filter label=com.docker.compose.project=sigsu-service --format 'table {{.Names}}\t{{.Status}}'; fi; curl -fsS "$base/actuator/health"; printf '\n'; } > "$out/compose-ps.txt" 2>&1
printf 'Evidencias verificadas en %s; cron por defecto restaurado.\n' "$out"
