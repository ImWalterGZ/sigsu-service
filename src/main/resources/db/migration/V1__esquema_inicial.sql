CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- seguridad
CREATE TABLE usuario (
    id uuid PRIMARY KEY,
    correo varchar(255) NOT NULL UNIQUE,
    nombre varchar(255) NOT NULL,
    contrasena varchar(255) NOT NULL,
    activo boolean NOT NULL
);
CREATE TABLE usuario_rol (
    usuario_id uuid REFERENCES usuario,
    rol varchar(30) NOT NULL CHECK (rol IN ('ESTUDIANTE','RESPONSABLE','ADMINISTRADOR')),
    PRIMARY KEY(usuario_id,rol)
);
-- catalogo
CREATE TABLE area (
    id uuid PRIMARY KEY,
    nombre varchar(255) NOT NULL,
    correo varchar(255) NOT NULL
);
CREATE TABLE categoria (
    id uuid PRIMARY KEY,
    nombre varchar(255) NOT NULL
);
-- seguridad: asignaciones
CREATE TABLE responsable_area (
    id uuid PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES usuario,
    area_id uuid NOT NULL REFERENCES area,
    asignada_en timestamptz NOT NULL,
    UNIQUE(usuario_id,area_id)
);
-- catalogo: servicios y formularios
CREATE TABLE servicio (
    id uuid PRIMARY KEY,
    nombre varchar(255) NOT NULL,
    descripcion text NOT NULL,
    area_id uuid NOT NULL REFERENCES area,
    categoria_id uuid NOT NULL REFERENCES categoria,
    plazo_dias_habiles integer NOT NULL CHECK(plazo_dias_habiles>0),
    version_vigente integer NOT NULL,
    activo boolean NOT NULL
);
CREATE TABLE formulario_servicio (
    servicio_id uuid REFERENCES servicio,
    version integer NOT NULL,
    esquema jsonb NOT NULL,
    creado_en timestamptz NOT NULL,
    PRIMARY KEY(servicio_id,version)
);
-- Diferida hasta COMMIT para insertar servicio y formulario vigente en la misma transacción.
ALTER TABLE servicio ADD CONSTRAINT formulario_vigente FOREIGN KEY(id,version_vigente) REFERENCES formulario_servicio(servicio_id,version) DEFERRABLE INITIALLY DEFERRED;
-- catalogo: calendario
CREATE TABLE dia_inhabil (
    fecha date PRIMARY KEY,
    motivo varchar(255) NOT NULL
);
CREATE SEQUENCE secuencia_folio;
-- solicitudes
-- La FK compuesta conserva la versión exacta del formulario usada al registrar.
CREATE TABLE solicitud (
    id uuid PRIMARY KEY,
    folio varchar(255) NOT NULL UNIQUE,
    estudiante_id uuid NOT NULL REFERENCES usuario,
    servicio_id uuid NOT NULL,
    formulario_version integer NOT NULL,
    datos jsonb NOT NULL,
    estado varchar(30) NOT NULL CHECK(estado IN ('REGISTRADA','SIN_RESPUESTA','EN_ATENCION','DEMORADO','ATENDIDA','RECHAZADA','CANCELADA')),
    responsable_id uuid REFERENCES usuario,
    vence_en timestamptz NOT NULL,
    creada_en timestamptz NOT NULL,
    cerrada_en timestamptz,
    version integer NOT NULL,
    FOREIGN KEY(servicio_id,formulario_version) REFERENCES formulario_servicio(servicio_id,version)
);
-- Evita duplicados incluso entre INSERT concurrentes; una consulta previa tendría una carrera.
-- Los estados cerrados liberan la combinación estudiante/servicio.
CREATE UNIQUE INDEX solicitud_abierta ON solicitud (estudiante_id, servicio_id) WHERE estado IN ('REGISTRADA','SIN_RESPUESTA','EN_ATENCION','DEMORADO');
CREATE TABLE historial (
    id uuid PRIMARY KEY,
    solicitud_id uuid NOT NULL REFERENCES solicitud,
    autor_id uuid REFERENCES usuario,
    anterior varchar(30),
    nuevo varchar(30) NOT NULL,
    observacion text,
    fecha timestamptz NOT NULL
);
CREATE TABLE adjunto (
    id uuid PRIMARY KEY,
    solicitud_id uuid NOT NULL REFERENCES solicitud,
    nombre varchar(255) NOT NULL,
    ruta varchar(255) NOT NULL,
    tipo varchar(255) NOT NULL,
    bytes bigint NOT NULL,
    creado_en timestamptz NOT NULL
);
-- administracion
CREATE TABLE parametro (
    clave varchar(255) PRIMARY KEY,
    valor varchar(255) NOT NULL
);
-- Trigger en vez de permisos: también protege cuando la aplicación es dueña de la tabla.
-- Se demuestra con psql ejecutando UPDATE, DELETE o TRUNCATE; no depende del rol conectado.
CREATE FUNCTION rechazar_historial() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'historial es de solo inserción'; END $$;
CREATE TRIGGER historial_inmutable BEFORE UPDATE OR DELETE ON historial FOR EACH ROW EXECUTE FUNCTION rechazar_historial();
CREATE TRIGGER historial_no_truncar BEFORE TRUNCATE ON historial FOR EACH STATEMENT EXECUTE FUNCTION rechazar_historial();
CREATE FUNCTION rechazar_formulario() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'formulario_servicio es de solo inserción'; END $$;
CREATE TRIGGER formulario_inmutable BEFORE UPDATE OR DELETE ON formulario_servicio FOR EACH ROW EXECUTE FUNCTION rechazar_formulario();
CREATE TRIGGER formulario_no_truncar BEFORE TRUNCATE ON formulario_servicio FOR EACH STATEMENT EXECUTE FUNCTION rechazar_formulario();
