#!/usr/bin/env python3
"""Demostración local con curl. Crea datos nuevos; no borra datos ni contenedores."""
import concurrent.futures
import datetime as fechas
import json
import os
import shlex
from pathlib import Path
import subprocess
import tempfile
import uuid
from zoneinfo import ZoneInfo

base = os.environ.get('SIGSU_URL', 'http://localhost:8080')
administrador = 'admin@utch.edu.mx:sigsu123'
responsable = 'resp.escolares@utch.edu.mx:sigsu123'
segundo = 'resp.escolares2@utch.edu.mx:sigsu123'
resultados = []

def peticion(nombre, metodo, ruta, esperado, usuario=None, datos=None, cabeceras=None, archivo=None):
    with tempfile.TemporaryDirectory() as temporal:
        cuerpo = Path(temporal) / 'cuerpo'
        encabezados = Path(temporal) / 'encabezados'
        comando = ['curl', '-sS', '-X', metodo, base + ruta, '-D', str(encabezados), '-o', str(cuerpo), '-w', '%{http_code}']
        if usuario:
            comando += ['-u', usuario]
        if datos is not None:
            comando += ['-H', 'Content-Type: application/json', '--data-binary', json.dumps(datos, ensure_ascii=False)]
        for cabecera in cabeceras or []:
            comando += ['-H', cabecera]
        if archivo:
            comando += ['-F', 'archivo=@' + str(archivo)]
        codigo = int(subprocess.check_output(comando, text=True))
        contenido = cuerpo.read_bytes()
        headers = encabezados.read_text()
    assert codigo == esperado, (nombre, codigo, contenido.decode(errors='replace'))
    try:
        valor = json.loads(contenido)
    except ValueError:
        valor = contenido.decode(errors='replace')
    resultados.append((nombre, metodo + ' ' + ruta, codigo, valor))
    print(nombre, codigo, flush=True)
    return valor, headers, contenido

def registrar(usuario, servicio='00000000-0000-0000-0000-000000000021', datos=None, cabeceras=None):
    if datos is None:
        datos = {'motivo': 'Solicitud de beca', 'conCalificaciones': True}
    return peticion('Registro', 'POST', '/solicitudes', 201, usuario, {'servicioId': servicio, 'datos': datos}, cabeceras)

salud, _, _ = peticion('Salud', 'GET', '/actuator/health', 200)
assert salud['status'] == 'UP'
peticion('Sin credenciales', 'GET', '/catalogo/servicios', 401)
servicios, _, _ = peticion('Catálogo', 'GET', '/catalogo/servicios', 200, administrador)
peticion('Detalle de servicio', 'GET', '/catalogo/servicios/00000000-0000-0000-0000-000000000021', 200, administrador)
sufijo = uuid.uuid4().hex[:10]
correo = 'prueba.' + sufijo + '@alumnos.utch.edu.mx'
usuario, _, _ = peticion('Crear usuario', 'POST', '/administracion/usuarios', 201, administrador, {'correo': correo, 'nombre': 'Estudiante de demostración', 'contrasena': 'sigsu123', 'roles': ['ESTUDIANTE']})
estudiante = correo + ':sigsu123'
peticion('Estudiante sin permiso en bandeja', 'GET', '/bandeja', 403, estudiante)
invalidos, _, _ = peticion('Formulario inválido', 'POST', '/solicitudes', 422, estudiante, {'servicioId': servicios[0]['id'], 'datos': {'motivo': 'x', 'conCalificaciones': True}})
assert any('motivo' in error['campo'] for error in invalidos['errores'])
solicitud, _, _ = registrar(estudiante)
folio = solicitud['folio']
dias, _, _ = peticion('Listar días inhábiles', 'GET', '/administracion/dias-inhabiles', 200, administrador)
inhabiles = {dia['fecha'] for dia in dias}
creada = fechas.datetime.fromisoformat(solicitud['creadaEn'].replace('Z', '+00:00')).astimezone(ZoneInfo('America/Chihuahua'))
vence = creada
restantes = 3
while restantes:
    vence += fechas.timedelta(days=1)
    if vence.weekday() < 5 and vence.date().isoformat() not in inhabiles:
        restantes -= 1
observado = fechas.datetime.fromisoformat(solicitud['venceEn'].replace('Z', '+00:00'))
assert abs((observado - vence).total_seconds()) < 0.001
resultados.append(('Vencimiento calculado', 'mismo horario; salta fines de semana e inhábiles', 200, {'creadaEn': solicitud['creadaEn'], 'venceEn': solicitud['venceEn']}))
duplicado, _, _ = peticion('Duplicado con folio', 'POST', '/solicitudes', 409, estudiante, {'servicioId': servicios[0]['id'], 'datos': {'motivo': 'Otro motivo', 'conCalificaciones': False}})
assert duplicado['folioExistente'] == folio
peticion('Solicitudes propias', 'GET', '/solicitudes', 200, estudiante)
peticion('Detalle con historial', 'GET', '/solicitudes/' + folio, 200, estudiante)
peticion('Otro estudiante no puede leer', 'GET', '/solicitudes/' + folio, 403, 'estudiante2@alumnos.utch.edu.mx:sigsu123')
peticion('Responsable de otra área no puede tomar', 'POST', '/solicitudes/' + folio + '/tomar', 403, 'resp.ti@utch.edu.mx:sigsu123', {'version': 0})
bandeja, _, _ = peticion('Bandeja ordenada', 'GET', '/bandeja', 200, responsable)
assert [s['venceEn'] for s in bandeja] == sorted(s['venceEn'] for s in bandeja)
peticion('Bandeja filtrada', 'GET', '/bandeja?estado=REGISTRADA', 200, responsable)
with tempfile.NamedTemporaryFile(mode='w', suffix='.txt') as adjunto:
    adjunto.write('Evidencia de solicitud SIGSU'); adjunto.flush()
    metadato, _, _ = peticion('Subir adjunto', 'POST', '/solicitudes/' + folio + '/adjuntos', 201, estudiante, archivo=adjunto.name)
    _, _, contenido = peticion('Descargar adjunto', 'GET', '/solicitudes/' + folio + '/adjuntos/' + metadato['id'], 200, estudiante)
    assert contenido == b'Evidencia de solicitud SIGSU'
tomada, _, _ = peticion('Tomar solicitud', 'POST', '/solicitudes/' + folio + '/tomar', 200, responsable, {'version': 0})
assert tomada['estado'] == 'EN_ATENCION' and tomada['version'] == 1
peticion('Segunda toma con misma versión', 'POST', '/solicitudes/' + folio + '/tomar', 409, segundo, {'version': 0})
peticion('Cancelar tomada', 'POST', '/solicitudes/' + folio + '/cancelar', 409, estudiante)
peticion('Otro responsable no puede cerrar', 'POST', '/solicitudes/' + folio + '/estado', 403, segundo, {'estado': 'ATENDIDA'})
peticion('Rechazar sin observación', 'POST', '/solicitudes/' + folio + '/estado', 422, responsable, {'estado': 'RECHAZADA'})
peticion('Atender', 'POST', '/solicitudes/' + folio + '/estado', 200, responsable, {'estado': 'ATENDIDA', 'observacion': 'Constancia entregada'})
# Mismo cuerpo, mismo usuario y misma llave: exactamente una inserción.
llave = 'Idempotency-Key: prueba-' + sufijo
idempotente, _, cuerpo1 = registrar(estudiante, cabeceras=[llave])
repetida, encabezados, cuerpo2 = registrar(estudiante, cabeceras=[llave])
assert cuerpo1 == cuerpo2 and 'idempotent-replayed: true' in encabezados.lower()
propias, _, _ = peticion('Una sola solicitud idempotente', 'GET', '/solicitudes', 200, estudiante)
assert sum(s['folio'] == idempotente['folio'] for s in propias) == 1
resultados.append(('Reproducción idempotente', 'POST /solicitudes', 201, {'folio': repetida['folio'], 'Idempotent-Replayed': True, 'cuerpoIdentico': True, 'inserciones': 1}))
peticion('Llave con otro cuerpo', 'POST', '/solicitudes', 422, estudiante, {'servicioId': servicios[0]['id'], 'datos': {'motivo': 'Otra solicitud', 'conCalificaciones': False}}, [llave])
peticion('Cancelar registrada', 'POST', '/solicitudes/' + idempotente['folio'] + '/cancelar', 200, estudiante)
rechazada, _, _ = registrar(estudiante)
peticion('Tomar para rechazar', 'POST', '/solicitudes/' + rechazada['folio'] + '/tomar', 200, responsable, {'version': 0})
peticion('Rechazar con observación', 'POST', '/solicitudes/' + rechazada['folio'] + '/estado', 200, responsable, {'estado': 'RECHAZADA', 'observacion': 'Documento incompleto'})
# Dos solicitudes en estados distintos para ambos vencimientos.
registrada, _, _ = registrar(estudiante)
ti, _, _ = registrar(estudiante, '00000000-0000-0000-0000-000000000022', {})
peticion('Tomar TI', 'POST', '/solicitudes/' + ti['folio'] + '/tomar', 200, 'resp.ti@utch.edu.mx:sigsu123', {'version': 0})
vencimientos, _, _ = peticion('Vencer ambas ramas', 'POST', '/administracion/vencimientos?ahora=2027-02-01T12:00:00Z', 200, administrador)
assert registrada['folio'] in vencimientos['foliosSinRespuesta'] and ti['folio'] in vencimientos['foliosDemoradas']
sinrespuesta, _, _ = peticion('Tomar SIN_RESPUESTA', 'POST', '/solicitudes/' + registrada['folio'] + '/tomar', 200, responsable, {'version': 1})
assert sinrespuesta['estado'] == 'DEMORADO'
# Servicio nuevo y sus formularios: no cambiar la semilla durante la demostración.
esquema = {'$schema': 'https://json-schema.org/draft/2020-12/schema', 'type': 'object', 'additionalProperties': False, 'properties': {}, 'required': []}
nuevo, _, _ = peticion('Crear servicio con v1', 'POST', '/administracion/servicios', 201, administrador, {'nombre': 'Trámite demostración ' + sufijo, 'descripcion': 'Prueba de versionado', 'areaId': '00000000-0000-0000-0000-000000000001', 'categoriaId': '00000000-0000-0000-0000-000000000011', 'plazoDiasHabiles': 2, 'esquema': esquema})
vieja, _, _ = registrar(estudiante, nuevo['id'], {})
esquema2 = dict(esquema, properties={'motivo': {'type': 'string'}}, required=['motivo'])
publicada, _, _ = peticion('Publicar formulario v2', 'POST', '/administracion/servicios/' + nuevo['id'] + '/formularios', 201, administrador, {'esquema': esquema2})
assert publicada['versionVigente'] == 2
detalle, _, _ = peticion('Solicitud conserva formulario v1', 'GET', '/solicitudes/' + vieja['folio'], 200, administrador)
assert detalle['solicitud']['formularioVersion'] == 1
peticion('Ajustar plazo y activo', 'PATCH', '/administracion/servicios/' + nuevo['id'], 200, administrador, {'plazoDiasHabiles': 4, 'activo': False})
peticion('Agregar día inhábil', 'POST', '/administracion/dias-inhabiles', 201, administrador, {'fecha': '2030-01-01', 'motivo': 'Demostración'})
peticion('Listar parámetros', 'GET', '/administracion/parametros', 200, administrador)
peticion('Cambiar parámetro', 'PUT', '/administracion/parametros/demostracion', 200, administrador, {'valor': sufijo})
responsable_nuevo, _, _ = peticion('Crear usuario multirrol', 'POST', '/administracion/usuarios', 201, administrador, {'correo': 'responsable.' + sufijo + '@utch.edu.mx', 'nombre': 'Responsable de demostración', 'contrasena': 'sigsu123', 'roles': ['ESTUDIANTE', 'RESPONSABLE']})
peticion('Asignar responsable de área', 'POST', '/administracion/usuarios/' + responsable_nuevo['id'] + '/areas', 204, administrador, {'areaId': '00000000-0000-0000-0000-000000000001'})
# Los avisos son asíncronos; las llamadas anteriores dan tiempo para su entrega.
mensajes = json.loads(subprocess.check_output(['curl', '-fsS', os.environ.get('SIGSU_MAILPIT_URL', 'http://localhost:8025') + '/api/v1/messages']))
assert mensajes['total'] > 0
resultados.append(('Correo en Mailpit', 'GET /api/v1/messages', 200, {'total': mensajes['total']}))
# Una toma simultánea real: solo una sentencia puede afectar la fila.
correo_carrera = 'carrera.' + sufijo + '@alumnos.utch.edu.mx'
peticion('Crear estudiante para carrera', 'POST', '/administracion/usuarios', 201, administrador, {'correo': correo_carrera, 'nombre': 'Estudiante concurrente', 'contrasena': 'sigsu123', 'roles': ['ESTUDIANTE']})
carrera, _, _ = registrar(correo_carrera + ':sigsu123')
def competir(credenciales):
    salida = subprocess.check_output(['curl', '-sS', '-u', credenciales, '-X', 'POST', base + '/solicitudes/' + carrera['folio'] + '/tomar', '-H', 'Content-Type: application/json', '-d', '{"version":0}', '-w', '\n%{http_code}'], text=True)
    cuerpo, codigo = salida.rsplit('\n', 1)
    return int(codigo), json.loads(cuerpo)
with concurrent.futures.ThreadPoolExecutor(max_workers=2) as ejecutor:
    tomas = list(ejecutor.map(competir, [responsable, segundo]))
assert sorted(codigo for codigo, _ in tomas) == [200, 409], tomas
resultados.append(('Toma simultánea', 'dos POST /solicitudes/' + carrera['folio'] + '/tomar con version=0', '200 y 409', tomas))
# Multipart idéntico, incluso su frontera MIME, para que la huella sea idéntica.
frontera = 'demostracion-sigsu'
cuerpo_multipart = '--' + frontera + '\r\nContent-Disposition: form-data; name="archivo"; filename="evidencia.txt"\r\nContent-Type: text/plain\r\n\r\nAdjunto idempotente\r\n--' + frontera + '--\r\n'
with tempfile.TemporaryDirectory() as temporal:
    archivo_multipart = Path(temporal) / 'multipart'
    archivo_multipart.write_bytes(cuerpo_multipart.encode())
    respuestas = []
    for intento in range(2):
        salida = subprocess.check_output(['curl', '-sS', '-u', correo_carrera + ':sigsu123', '-X', 'POST', base + '/solicitudes/' + carrera['folio'] + '/adjuntos', '-H', 'Content-Type: multipart/form-data; boundary=' + frontera, '-H', 'Idempotency-Key: adjunto-' + sufijo, '--data-binary', '@' + str(archivo_multipart), '-i'], text=True)
        encabezados_multipart, respuesta_multipart = salida.split('\n\n', 1)
        assert '201' in encabezados_multipart.splitlines()[0], salida
        respuestas.append(respuesta_multipart)
    assert respuestas[0] == respuestas[1] and 'idempotent-replayed: true' in encabezados_multipart.lower()
    resultados.append(('Multipart idempotente', 'POST /solicitudes/' + carrera['folio'] + '/adjuntos dos veces', 201, {'adjunto': json.loads(respuestas[1]), 'Idempotent-Replayed': True}))
peticion('OpenAPI público', 'GET', '/v3/api-docs', 200)
# psql rechaza modificaciones aun usando la cuenta dueña de las tablas.
for nombre, sentencia in [('Trigger historial UPDATE', "UPDATE historial SET observacion='x'"), ('Trigger historial DELETE', 'DELETE FROM historial'), ('Trigger historial TRUNCATE', 'TRUNCATE historial'), ('Trigger formulario', 'UPDATE formulario_servicio SET version=version')]:
    proceso = subprocess.run(shlex.split(os.environ.get('COMPOSE', 'docker compose')) + ['exec', '-T', 'postgres', 'psql', '-U', 'sigsu', '-d', 'sigsu', '-c', sentencia], capture_output=True, text=True)
    assert proceso.returncode != 0 and 'es de solo inserción' in proceso.stderr, proceso.stderr
    resultados.append((nombre, sentencia, 'ERROR esperado', proceso.stderr[proceso.stderr.index('ERROR:'):].strip()))
texto = 'Verificación local\n\nEjecutada con python3 scripts/verificar.py el ' + fechas.datetime.now().astimezone().isoformat() + '. Cada petición HTTP se ejecutó mediante curl; el script comprueba status y contenido.\n\n'
for nombre, ruta, codigo, resultado in resultados:
    if nombre == 'OpenAPI público':
        resultado = {'openapi': resultado.get('openapi'), 'rutas': len(resultado.get('paths', {}))}
    if nombre == 'Catálogo':
        resultado = [{'id': s['id'], 'nombre': s['nombre']} for s in resultado]
    texto += nombre + '\n' + ruta + ' → ' + str(codigo) + '\n\n' + json.dumps(resultado, ensure_ascii=False, indent=2) + '\n\n'
Path('docs/evidencias').mkdir(parents=True, exist_ok=True)
Path('docs/evidencias/verificacion.txt').write_text(texto)
print('Verificación completa; evidencia en docs/evidencias/verificacion.txt')
