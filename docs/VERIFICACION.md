# Verificación local

Ejecutada con `python3 scripts/verificar.py` el 2026-10-28T21:09:55.440265-06:00. Cada petición HTTP se ejecutó mediante curl; el script comprueba status y contenido.

## Salud

`GET /actuator/health` → **200**

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

## Sin credenciales

`GET /catalogo/servicios` → **401**

```json
{
  "status": 401,
  "mensaje": "credenciales requeridas o inválidas",
  "folioExistente": null,
  "errores": []
}
```

## Catálogo

`GET /catalogo/servicios` → **200**

```json
[
  {
    "id": "00000000-0000-0000-0000-000000000021",
    "nombre": "Constancia de estudios"
  },
  {
    "id": "00000000-0000-0000-0000-000000000022",
    "nombre": "Restablecer contraseña del correo institucional"
  },
  {
    "id": "00000000-0000-0000-0000-000000000023",
    "nombre": "Préstamo de laboratorio"
  }
]
```

## Detalle de servicio

`GET /catalogo/servicios/00000000-0000-0000-0000-000000000021` → **200**

```json
{
  "id": "00000000-0000-0000-0000-000000000021",
  "nombre": "Constancia de estudios",
  "descripcion": "Trámite universitario",
  "areaId": "00000000-0000-0000-0000-000000000001",
  "correoArea": "escolares@utch.edu.mx",
  "categoriaId": "00000000-0000-0000-0000-000000000011",
  "plazoDiasHabiles": 3,
  "versionVigente": 1,
  "activo": true,
  "esquema": {
    "type": "object",
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "required": [
      "motivo",
      "conCalificaciones"
    ],
    "properties": {
      "motivo": {
        "type": "string",
        "minLength": 5
      },
      "conCalificaciones": {
        "type": "boolean"
      }
    },
    "additionalProperties": false
  }
}
```

## Crear usuario

`POST /administracion/usuarios` → **201**

```json
{
  "id": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "correo": "prueba.94d9732a8c@alumnos.utch.edu.mx",
  "nombre": "Estudiante de demostración",
  "roles": [
    "ESTUDIANTE"
  ],
  "activo": true
}
```

## Estudiante sin permiso en bandeja

`GET /bandeja` → **403**

```json
{
  "status": 403,
  "mensaje": "sin permiso para esta operación",
  "folioExistente": null,
  "errores": []
}
```

## Formulario inválido

`POST /solicitudes` → **422**

```json
{
  "status": 422,
  "mensaje": "formulario inválido",
  "folioExistente": null,
  "errores": [
    {
      "campo": "/motivo",
      "mensaje": "/motivo: debe tener al menos 5 caracteres"
    }
  ]
}
```

## Registro

`POST /solicitudes` → **201**

```json
{
  "id": "fe83398e-34e7-4d24-8d44-84baacd9d0ba",
  "folio": "SIGSU-2026-000008",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "REGISTRADA",
  "responsableId": null,
  "venceEn": "2026-11-04T03:09:50.526928628Z",
  "creadaEn": "2026-10-29T03:09:50.526928628Z",
  "cerradaEn": null,
  "version": 0
}
```

## Listar días inhábiles

`GET /administracion/dias-inhabiles` → **200**

```json
[
  {
    "fecha": "2026-11-02",
    "motivo": "Día de Muertos"
  },
  {
    "fecha": "2026-11-16",
    "motivo": "Revolución"
  },
  {
    "fecha": "2026-12-25",
    "motivo": "Navidad"
  },
  {
    "fecha": "2027-01-01",
    "motivo": "Año Nuevo"
  },
  {
    "fecha": "2030-01-01",
    "motivo": "Demostración"
  }
]
```

## Vencimiento calculado

`mismo horario; salta fines de semana e inhábiles` → **200**

```json
{
  "creadaEn": "2026-10-29T03:09:50.526928628Z",
  "venceEn": "2026-11-04T03:09:50.526928628Z"
}
```

## Duplicado con folio

`POST /solicitudes` → **409**

```json
{
  "status": 409,
  "mensaje": "ya tienes SIGSU-2026-000008 en curso",
  "folioExistente": "SIGSU-2026-000008",
  "errores": []
}
```

## Solicitudes propias

`GET /solicitudes` → **200**

```json
[
  {
    "id": "fe83398e-34e7-4d24-8d44-84baacd9d0ba",
    "folio": "SIGSU-2026-000008",
    "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
    "servicioId": "00000000-0000-0000-0000-000000000021",
    "formularioVersion": 1,
    "datos": {
      "motivo": "Solicitud de beca",
      "conCalificaciones": true
    },
    "estado": "REGISTRADA",
    "responsableId": null,
    "venceEn": "2026-11-04T03:09:50.526929Z",
    "creadaEn": "2026-10-29T03:09:50.526929Z",
    "cerradaEn": null,
    "version": 0
  }
]
```

## Detalle con historial

`GET /solicitudes/SIGSU-2026-000008` → **200**

```json
{
  "solicitud": {
    "id": "fe83398e-34e7-4d24-8d44-84baacd9d0ba",
    "folio": "SIGSU-2026-000008",
    "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
    "servicioId": "00000000-0000-0000-0000-000000000021",
    "formularioVersion": 1,
    "datos": {
      "motivo": "Solicitud de beca",
      "conCalificaciones": true
    },
    "estado": "REGISTRADA",
    "responsableId": null,
    "venceEn": "2026-11-04T03:09:50.526929Z",
    "creadaEn": "2026-10-29T03:09:50.526929Z",
    "cerradaEn": null,
    "version": 0
  },
  "historial": [
    {
      "autorId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
      "anterior": null,
      "nuevo": "REGISTRADA",
      "observacion": "registro",
      "fecha": "2026-10-29T03:09:50.526929Z"
    }
  ],
  "adjuntos": []
}
```

## Otro estudiante no puede leer

`GET /solicitudes/SIGSU-2026-000008` → **403**

```json
{
  "status": 403,
  "mensaje": "sin permiso para consultar esta solicitud",
  "folioExistente": null,
  "errores": []
}
```

## Responsable de otra área no puede tomar

`POST /solicitudes/SIGSU-2026-000008/tomar` → **403**

```json
{
  "status": 403,
  "mensaje": "no perteneces al área del servicio",
  "folioExistente": null,
  "errores": []
}
```

## Bandeja ordenada

`GET /bandeja` → **200**

```json
[
  {
    "id": "2f1cf4ff-66d6-47d0-bcb3-2ff1c1e59633",
    "folio": "SIGSU-2026-000007",
    "estudianteId": "8d6f78a3-6223-493b-ae64-efe83b78c2b4",
    "servicioId": "6def6de5-bd20-456b-a810-b6bec229eda8",
    "formularioVersion": 1,
    "datos": {},
    "estado": "REGISTRADA",
    "responsableId": null,
    "venceEn": "2026-10-31T03:06:31.026557Z",
    "creadaEn": "2026-10-29T03:06:31.026557Z",
    "cerradaEn": null,
    "version": 0
  },
  {
    "id": "5d775962-290e-4ecf-a767-5dde8340db21",
    "folio": "SIGSU-2026-000005",
    "estudianteId": "8d6f78a3-6223-493b-ae64-efe83b78c2b4",
    "servicioId": "00000000-0000-0000-0000-000000000021",
    "formularioVersion": 1,
    "datos": {
      "motivo": "Solicitud de beca",
      "conCalificaciones": true
    },
    "estado": "DEMORADO",
    "responsableId": "00000000-0000-0000-0000-000000000103",
    "venceEn": "2026-11-04T03:06:30.444662Z",
    "creadaEn": "2026-10-29T03:06:30.444662Z",
    "cerradaEn": null,
    "version": 2
  },
  {
    "id": "fe83398e-34e7-4d24-8d44-84baacd9d0ba",
    "folio": "SIGSU-2026-000008",
    "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
    "servicioId": "00000000-0000-0000-0000-000000000021",
    "formularioVersion": 1,
    "datos": {
      "motivo": "Solicitud de beca",
      "conCalificaciones": true
    },
    "estado": "REGISTRADA",
    "responsableId": null,
    "venceEn": "2026-11-04T03:09:50.526929Z",
    "creadaEn": "2026-10-29T03:09:50.526929Z",
    "cerradaEn": null,
    "version": 0
  }
]
```

## Bandeja filtrada

`GET /bandeja?estado=REGISTRADA` → **200**

```json
[
  {
    "id": "2f1cf4ff-66d6-47d0-bcb3-2ff1c1e59633",
    "folio": "SIGSU-2026-000007",
    "estudianteId": "8d6f78a3-6223-493b-ae64-efe83b78c2b4",
    "servicioId": "6def6de5-bd20-456b-a810-b6bec229eda8",
    "formularioVersion": 1,
    "datos": {},
    "estado": "REGISTRADA",
    "responsableId": null,
    "venceEn": "2026-10-31T03:06:31.026557Z",
    "creadaEn": "2026-10-29T03:06:31.026557Z",
    "cerradaEn": null,
    "version": 0
  },
  {
    "id": "fe83398e-34e7-4d24-8d44-84baacd9d0ba",
    "folio": "SIGSU-2026-000008",
    "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
    "servicioId": "00000000-0000-0000-0000-000000000021",
    "formularioVersion": 1,
    "datos": {
      "motivo": "Solicitud de beca",
      "conCalificaciones": true
    },
    "estado": "REGISTRADA",
    "responsableId": null,
    "venceEn": "2026-11-04T03:09:50.526929Z",
    "creadaEn": "2026-10-29T03:09:50.526929Z",
    "cerradaEn": null,
    "version": 0
  }
]
```

## Subir adjunto

`POST /solicitudes/SIGSU-2026-000008/adjuntos` → **201**

```json
{
  "id": "db5de31e-1872-4be7-9c86-3c34b4f38695",
  "nombre": "tmpr0w4jvf_.txt",
  "tipo": "text/plain",
  "bytes": 28
}
```

## Descargar adjunto

`GET /solicitudes/SIGSU-2026-000008/adjuntos/db5de31e-1872-4be7-9c86-3c34b4f38695` → **200**

```json
"Evidencia de solicitud SIGSU"
```

## Tomar solicitud

`POST /solicitudes/SIGSU-2026-000008/tomar` → **200**

```json
{
  "id": "fe83398e-34e7-4d24-8d44-84baacd9d0ba",
  "folio": "SIGSU-2026-000008",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "EN_ATENCION",
  "responsableId": "00000000-0000-0000-0000-000000000103",
  "venceEn": "2026-11-04T03:09:50.526929Z",
  "creadaEn": "2026-10-29T03:09:50.526929Z",
  "cerradaEn": null,
  "version": 1
}
```

## Segunda toma con misma versión

`POST /solicitudes/SIGSU-2026-000008/tomar` → **409**

```json
{
  "status": 409,
  "mensaje": "otro responsable la tomó primero o el estado no permite tomar",
  "folioExistente": null,
  "errores": []
}
```

## Cancelar tomada

`POST /solicitudes/SIGSU-2026-000008/cancelar` → **409**

```json
{
  "status": 409,
  "mensaje": "solo se cancela una solicitud REGISTRADA",
  "folioExistente": null,
  "errores": []
}
```

## Otro responsable no puede cerrar

`POST /solicitudes/SIGSU-2026-000008/estado` → **403**

```json
{
  "status": 403,
  "mensaje": "solo puede cerrar quien tomó la solicitud",
  "folioExistente": null,
  "errores": []
}
```

## Rechazar sin observación

`POST /solicitudes/SIGSU-2026-000008/estado` → **422**

```json
{
  "status": 422,
  "mensaje": "rechazar exige observación",
  "folioExistente": null,
  "errores": []
}
```

## Atender

`POST /solicitudes/SIGSU-2026-000008/estado` → **200**

```json
{
  "id": "fe83398e-34e7-4d24-8d44-84baacd9d0ba",
  "folio": "SIGSU-2026-000008",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "ATENDIDA",
  "responsableId": "00000000-0000-0000-0000-000000000103",
  "venceEn": "2026-11-04T03:09:50.526929Z",
  "creadaEn": "2026-10-29T03:09:50.526929Z",
  "cerradaEn": "2026-10-29T03:09:52.004249804Z",
  "version": 2
}
```

## Registro

`POST /solicitudes` → **201**

```json
{
  "id": "16f36230-b226-474a-b231-e04490e77609",
  "folio": "SIGSU-2026-000010",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "REGISTRADA",
  "responsableId": null,
  "venceEn": "2026-11-04T03:09:52.129181439Z",
  "creadaEn": "2026-10-29T03:09:52.129181439Z",
  "cerradaEn": null,
  "version": 0
}
```

## Registro

`POST /solicitudes` → **201**

```json
{
  "id": "16f36230-b226-474a-b231-e04490e77609",
  "folio": "SIGSU-2026-000010",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "REGISTRADA",
  "responsableId": null,
  "venceEn": "2026-11-04T03:09:52.129181439Z",
  "creadaEn": "2026-10-29T03:09:52.129181439Z",
  "cerradaEn": null,
  "version": 0
}
```

## Una sola solicitud idempotente

`GET /solicitudes` → **200**

```json
[
  {
    "id": "16f36230-b226-474a-b231-e04490e77609",
    "folio": "SIGSU-2026-000010",
    "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
    "servicioId": "00000000-0000-0000-0000-000000000021",
    "formularioVersion": 1,
    "datos": {
      "motivo": "Solicitud de beca",
      "conCalificaciones": true
    },
    "estado": "REGISTRADA",
    "responsableId": null,
    "venceEn": "2026-11-04T03:09:52.129181Z",
    "creadaEn": "2026-10-29T03:09:52.129181Z",
    "cerradaEn": null,
    "version": 0
  },
  {
    "id": "fe83398e-34e7-4d24-8d44-84baacd9d0ba",
    "folio": "SIGSU-2026-000008",
    "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
    "servicioId": "00000000-0000-0000-0000-000000000021",
    "formularioVersion": 1,
    "datos": {
      "motivo": "Solicitud de beca",
      "conCalificaciones": true
    },
    "estado": "ATENDIDA",
    "responsableId": "00000000-0000-0000-0000-000000000103",
    "venceEn": "2026-11-04T03:09:50.526929Z",
    "creadaEn": "2026-10-29T03:09:50.526929Z",
    "cerradaEn": "2026-10-29T03:09:52.004250Z",
    "version": 2
  }
]
```

## Reproducción idempotente

`POST /solicitudes` → **201**

```json
{
  "folio": "SIGSU-2026-000010",
  "Idempotent-Replayed": true,
  "cuerpoIdentico": true,
  "inserciones": 1
}
```

## Llave con otro cuerpo

`POST /solicitudes` → **422**

```json
{
  "status": 422,
  "mensaje": "Idempotency-Key reutilizada con otra petición",
  "folioExistente": null,
  "errores": []
}
```

## Cancelar registrada

`POST /solicitudes/SIGSU-2026-000010/cancelar` → **200**

```json
{
  "id": "16f36230-b226-474a-b231-e04490e77609",
  "folio": "SIGSU-2026-000010",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "CANCELADA",
  "responsableId": null,
  "venceEn": "2026-11-04T03:09:52.129181Z",
  "creadaEn": "2026-10-29T03:09:52.129181Z",
  "cerradaEn": "2026-10-29T03:09:52.457838350Z",
  "version": 1
}
```

## Registro

`POST /solicitudes` → **201**

```json
{
  "id": "4e73d3e6-0f37-4bd4-b52d-17da865f5576",
  "folio": "SIGSU-2026-000011",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "REGISTRADA",
  "responsableId": null,
  "venceEn": "2026-11-04T03:09:52.538112916Z",
  "creadaEn": "2026-10-29T03:09:52.538112916Z",
  "cerradaEn": null,
  "version": 0
}
```

## Tomar para rechazar

`POST /solicitudes/SIGSU-2026-000011/tomar` → **200**

```json
{
  "id": "4e73d3e6-0f37-4bd4-b52d-17da865f5576",
  "folio": "SIGSU-2026-000011",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "EN_ATENCION",
  "responsableId": "00000000-0000-0000-0000-000000000103",
  "venceEn": "2026-11-04T03:09:52.538113Z",
  "creadaEn": "2026-10-29T03:09:52.538113Z",
  "cerradaEn": null,
  "version": 1
}
```

## Rechazar con observación

`POST /solicitudes/SIGSU-2026-000011/estado` → **200**

```json
{
  "id": "4e73d3e6-0f37-4bd4-b52d-17da865f5576",
  "folio": "SIGSU-2026-000011",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "RECHAZADA",
  "responsableId": "00000000-0000-0000-0000-000000000103",
  "venceEn": "2026-11-04T03:09:52.538113Z",
  "creadaEn": "2026-10-29T03:09:52.538113Z",
  "cerradaEn": "2026-10-29T03:09:52.703623928Z",
  "version": 2
}
```

## Registro

`POST /solicitudes` → **201**

```json
{
  "id": "b2aafe44-a56d-4b26-b92e-8d346c5801df",
  "folio": "SIGSU-2026-000012",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "REGISTRADA",
  "responsableId": null,
  "venceEn": "2026-11-04T03:09:52.784092344Z",
  "creadaEn": "2026-10-29T03:09:52.784092344Z",
  "cerradaEn": null,
  "version": 0
}
```

## Registro

`POST /solicitudes` → **201**

```json
{
  "id": "f374402e-9abf-4b93-a2b6-cb31551e2de4",
  "folio": "SIGSU-2026-000013",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000022",
  "formularioVersion": 1,
  "datos": {},
  "estado": "REGISTRADA",
  "responsableId": null,
  "venceEn": "2026-10-30T03:09:52.867746160Z",
  "creadaEn": "2026-10-29T03:09:52.867746160Z",
  "cerradaEn": null,
  "version": 0
}
```

## Tomar TI

`POST /solicitudes/SIGSU-2026-000013/tomar` → **200**

```json
{
  "id": "f374402e-9abf-4b93-a2b6-cb31551e2de4",
  "folio": "SIGSU-2026-000013",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000022",
  "formularioVersion": 1,
  "datos": {},
  "estado": "EN_ATENCION",
  "responsableId": "00000000-0000-0000-0000-000000000105",
  "venceEn": "2026-10-30T03:09:52.867746Z",
  "creadaEn": "2026-10-29T03:09:52.867746Z",
  "cerradaEn": null,
  "version": 1
}
```

## Vencer ambas ramas

`POST /administracion/vencimientos?ahora=2027-02-01T12:00:00Z` → **200**

```json
{
  "sinRespuesta": 2,
  "demoradas": 1,
  "foliosSinRespuesta": [
    "SIGSU-2026-000007",
    "SIGSU-2026-000012"
  ],
  "foliosDemoradas": [
    "SIGSU-2026-000013"
  ]
}
```

## Tomar SIN_RESPUESTA

`POST /solicitudes/SIGSU-2026-000012/tomar` → **200**

```json
{
  "id": "b2aafe44-a56d-4b26-b92e-8d346c5801df",
  "folio": "SIGSU-2026-000012",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "DEMORADO",
  "responsableId": "00000000-0000-0000-0000-000000000103",
  "venceEn": "2026-11-04T03:09:52.784092Z",
  "creadaEn": "2026-10-29T03:09:52.784092Z",
  "cerradaEn": null,
  "version": 2
}
```

## Crear servicio con v1

`POST /administracion/servicios` → **201**

```json
{
  "id": "b2411be0-21d0-4df8-9212-9bcbbd313711",
  "nombre": "Trámite demostración 94d9732a8c",
  "descripcion": "Prueba de versionado",
  "areaId": "00000000-0000-0000-0000-000000000001",
  "correoArea": "escolares@utch.edu.mx",
  "categoriaId": "00000000-0000-0000-0000-000000000011",
  "plazoDiasHabiles": 2,
  "versionVigente": 1,
  "activo": true,
  "esquema": {
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "type": "object",
    "additionalProperties": false,
    "properties": {},
    "required": []
  }
}
```

## Registro

`POST /solicitudes` → **201**

```json
{
  "id": "a66d311d-77c4-4554-95af-78e711a7876f",
  "folio": "SIGSU-2026-000014",
  "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
  "servicioId": "b2411be0-21d0-4df8-9212-9bcbbd313711",
  "formularioVersion": 1,
  "datos": {},
  "estado": "REGISTRADA",
  "responsableId": null,
  "venceEn": "2026-10-31T03:09:53.341678617Z",
  "creadaEn": "2026-10-29T03:09:53.341678617Z",
  "cerradaEn": null,
  "version": 0
}
```

## Publicar formulario v2

`POST /administracion/servicios/b2411be0-21d0-4df8-9212-9bcbbd313711/formularios` → **201**

```json
{
  "id": "b2411be0-21d0-4df8-9212-9bcbbd313711",
  "nombre": "Trámite demostración 94d9732a8c",
  "descripcion": "Prueba de versionado",
  "areaId": "00000000-0000-0000-0000-000000000001",
  "correoArea": "escolares@utch.edu.mx",
  "categoriaId": "00000000-0000-0000-0000-000000000011",
  "plazoDiasHabiles": 2,
  "versionVigente": 2,
  "activo": true,
  "esquema": {
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "motivo": {
        "type": "string"
      }
    },
    "required": [
      "motivo"
    ]
  }
}
```

## Solicitud conserva formulario v1

`GET /solicitudes/SIGSU-2026-000014` → **200**

```json
{
  "solicitud": {
    "id": "a66d311d-77c4-4554-95af-78e711a7876f",
    "folio": "SIGSU-2026-000014",
    "estudianteId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
    "servicioId": "b2411be0-21d0-4df8-9212-9bcbbd313711",
    "formularioVersion": 1,
    "datos": {},
    "estado": "REGISTRADA",
    "responsableId": null,
    "venceEn": "2026-10-31T03:09:53.341679Z",
    "creadaEn": "2026-10-29T03:09:53.341679Z",
    "cerradaEn": null,
    "version": 0
  },
  "historial": [
    {
      "autorId": "ff3c1743-2400-442d-9631-5ff7fd1dd33b",
      "anterior": null,
      "nuevo": "REGISTRADA",
      "observacion": "registro",
      "fecha": "2026-10-29T03:09:53.341679Z"
    }
  ],
  "adjuntos": []
}
```

## Ajustar plazo y activo

`PATCH /administracion/servicios/b2411be0-21d0-4df8-9212-9bcbbd313711` → **200**

```json
{
  "id": "b2411be0-21d0-4df8-9212-9bcbbd313711",
  "nombre": "Trámite demostración 94d9732a8c",
  "descripcion": "Prueba de versionado",
  "areaId": "00000000-0000-0000-0000-000000000001",
  "correoArea": "escolares@utch.edu.mx",
  "categoriaId": "00000000-0000-0000-0000-000000000011",
  "plazoDiasHabiles": 4,
  "versionVigente": 2,
  "activo": false,
  "esquema": {
    "type": "object",
    "$schema": "https://json-schema.org/draft/2020-12/schema",
    "required": [
      "motivo"
    ],
    "properties": {
      "motivo": {
        "type": "string"
      }
    },
    "additionalProperties": false
  }
}
```

## Agregar día inhábil

`POST /administracion/dias-inhabiles` → **201**

```json
{
  "fecha": "2030-01-01",
  "motivo": "Demostración"
}
```

## Listar parámetros

`GET /administracion/parametros` → **200**

```json
{
  "correo.remitente": "sigsu@utch.edu.mx",
  "demostracion": "bad48578e4",
  "zona.horaria": "America/Chihuahua"
}
```

## Cambiar parámetro

`PUT /administracion/parametros/demostracion` → **200**

```json
{
  "valor": "94d9732a8c"
}
```

## Crear usuario multirrol

`POST /administracion/usuarios` → **201**

```json
{
  "id": "ca5b8d76-fe49-4b82-8ba6-5bcff7603dad",
  "correo": "responsable.94d9732a8c@utch.edu.mx",
  "nombre": "Responsable de demostración",
  "roles": [
    "ESTUDIANTE",
    "RESPONSABLE"
  ],
  "activo": true
}
```

## Asignar responsable de área

`POST /administracion/usuarios/ca5b8d76-fe49-4b82-8ba6-5bcff7603dad/areas` → **204**

```json
""
```

## Correo en Mailpit

`GET /api/v1/messages` → **200**

```json
{
  "total": 31
}
```

## Crear estudiante para carrera

`POST /administracion/usuarios` → **201**

```json
{
  "id": "68b41787-4919-4a1a-8e25-5ffa5e58f453",
  "correo": "carrera.94d9732a8c@alumnos.utch.edu.mx",
  "nombre": "Estudiante concurrente",
  "roles": [
    "ESTUDIANTE"
  ],
  "activo": true
}
```

## Registro

`POST /solicitudes` → **201**

```json
{
  "id": "fa245d6d-59d9-460d-9ca4-e87a32c90243",
  "folio": "SIGSU-2026-000015",
  "estudianteId": "68b41787-4919-4a1a-8e25-5ffa5e58f453",
  "servicioId": "00000000-0000-0000-0000-000000000021",
  "formularioVersion": 1,
  "datos": {
    "motivo": "Solicitud de beca",
    "conCalificaciones": true
  },
  "estado": "REGISTRADA",
  "responsableId": null,
  "venceEn": "2026-11-04T03:09:54.310428846Z",
  "creadaEn": "2026-10-29T03:09:54.310428846Z",
  "cerradaEn": null,
  "version": 0
}
```

## Toma simultánea

`dos POST /solicitudes/SIGSU-2026-000015/tomar con version=0` → **200 y 409**

```json
[
  [
    200,
    {
      "id": "fa245d6d-59d9-460d-9ca4-e87a32c90243",
      "folio": "SIGSU-2026-000015",
      "estudianteId": "68b41787-4919-4a1a-8e25-5ffa5e58f453",
      "servicioId": "00000000-0000-0000-0000-000000000021",
      "formularioVersion": 1,
      "datos": {
        "motivo": "Solicitud de beca",
        "conCalificaciones": true
      },
      "estado": "EN_ATENCION",
      "responsableId": "00000000-0000-0000-0000-000000000103",
      "venceEn": "2026-11-04T03:09:54.310429Z",
      "creadaEn": "2026-10-29T03:09:54.310429Z",
      "cerradaEn": null,
      "version": 1
    }
  ],
  [
    409,
    {
      "status": 409,
      "mensaje": "otro responsable la tomó primero o el estado no permite tomar",
      "folioExistente": null,
      "errores": []
    }
  ]
]
```

## Multipart idempotente

`POST /solicitudes/SIGSU-2026-000015/adjuntos dos veces` → **201**

```json
{
  "adjunto": {
    "id": "8773e782-9c42-481f-8f03-b2ec572ab861",
    "nombre": "evidencia.txt",
    "tipo": "text/plain",
    "bytes": 19
  },
  "Idempotent-Replayed": true
}
```

## OpenAPI público

`GET /v3/api-docs` → **200**

```json
{
  "openapi": "3.1.0",
  "rutas": 19
}
```

## Trigger historial UPDATE

`UPDATE historial SET observacion='x'` → **ERROR esperado**

```json
"ERROR:  historial es de solo inserción\nCONTEXT:  PL/pgSQL function rechazar_historial() line 1 at RAISE\nError: executing /home/gzzz/.docker/cli-plugins/docker-compose exec -T postgres psql -U sigsu -d sigsu -c UPDATE historial SET observacion='x': exit status 1"
```

## Trigger historial DELETE

`DELETE FROM historial` → **ERROR esperado**

```json
"ERROR:  historial es de solo inserción\nCONTEXT:  PL/pgSQL function rechazar_historial() line 1 at RAISE\nError: executing /home/gzzz/.docker/cli-plugins/docker-compose exec -T postgres psql -U sigsu -d sigsu -c DELETE FROM historial: exit status 1"
```

## Trigger historial TRUNCATE

`TRUNCATE historial` → **ERROR esperado**

```json
"ERROR:  historial es de solo inserción\nCONTEXT:  PL/pgSQL function rechazar_historial() line 1 at RAISE\nError: executing /home/gzzz/.docker/cli-plugins/docker-compose exec -T postgres psql -U sigsu -d sigsu -c TRUNCATE historial: exit status 1"
```

## Trigger formulario

`UPDATE formulario_servicio SET version=version` → **ERROR esperado**

```json
"ERROR:  formulario_servicio es de solo inserción\nCONTEXT:  PL/pgSQL function rechazar_formulario() line 1 at RAISE\nError: executing /home/gzzz/.docker/cli-plugins/docker-compose exec -T postgres psql -U sigsu -d sigsu -c UPDATE formulario_servicio SET version=version: exit status 1"
```

## Pruebas unitarias y estructura

`export JAVA_HOME=/tmp/sigsu-jdk; ./mvnw -q test` terminó con código 0: **18 pruebas, 0 fallos, 0 errores**, sin contenedores como requisito de las pruebas. JUnit Jupiter 5.13.4 y Mockito 5.23.0.

Se comprobó la existencia de los 38 nombres de clase/interfaz/enum exigidos y que Solicitudes no importa entidades/repositorios de Catálogo ni clases de Notificaciones. El repositorio no tiene remoto.

## Imagen final

`podman compose up -d --build` terminó con código 0 después de fijar JUnit 5 y configurar Swagger Basic. Comprobación posterior: `/actuator/health` respondió 200 y `UP`; `/v3/api-docs` respondió 200 con 19 rutas y el esquema `autenticacionBasica` de tipo HTTP Basic.

## Verificación complementaria con Postman y evidencias directas

La colección de `postman/` pasó dos veces consecutivas: 48 requests y 101 assertions por corrida, sin fallos. La primera ejecución usó volúmenes nuevos y la segunda no reinició ningún servicio. `docs/evidencias/README.md` enumera los reportes JSON/JUnit/CLI, las pruebas directas de PostgreSQL y Redis, las carreras reales con curl, los correos y el vencimiento por scheduler. Las 18 pruebas unitarias siguen pasando. El esquema antes/después de reformatear V1 es idéntico y su diff está vacío. No se corrigió ningún bug de la aplicación. La app queda healthy con el cron predeterminado.
