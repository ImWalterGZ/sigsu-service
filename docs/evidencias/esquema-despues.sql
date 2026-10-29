


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;



COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';



CREATE FUNCTION public.rechazar_formulario() RETURNS trigger
    LANGUAGE plpgsql
    AS $$ BEGIN RAISE EXCEPTION 'formulario_servicio es de solo inserción'; END $$;



CREATE FUNCTION public.rechazar_historial() RETURNS trigger
    LANGUAGE plpgsql
    AS $$ BEGIN RAISE EXCEPTION 'historial es de solo inserción'; END $$;


SET default_tablespace = '';

SET default_table_access_method = heap;


CREATE TABLE public.adjunto (
    id uuid NOT NULL,
    solicitud_id uuid NOT NULL,
    nombre character varying(255) NOT NULL,
    ruta character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    bytes bigint NOT NULL,
    creado_en timestamp with time zone NOT NULL
);



CREATE TABLE public.area (
    id uuid NOT NULL,
    nombre character varying(255) NOT NULL,
    correo character varying(255) NOT NULL
);



CREATE TABLE public.categoria (
    id uuid NOT NULL,
    nombre character varying(255) NOT NULL
);



CREATE TABLE public.dia_inhabil (
    fecha date NOT NULL,
    motivo character varying(255) NOT NULL
);



CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);



CREATE TABLE public.formulario_servicio (
    servicio_id uuid NOT NULL,
    version integer NOT NULL,
    esquema jsonb NOT NULL,
    creado_en timestamp with time zone NOT NULL
);



CREATE TABLE public.historial (
    id uuid NOT NULL,
    solicitud_id uuid NOT NULL,
    autor_id uuid,
    anterior character varying(30),
    nuevo character varying(30) NOT NULL,
    observacion text,
    fecha timestamp with time zone NOT NULL
);



CREATE TABLE public.parametro (
    clave character varying(255) NOT NULL,
    valor character varying(255) NOT NULL
);



CREATE TABLE public.responsable_area (
    id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    area_id uuid NOT NULL,
    asignada_en timestamp with time zone NOT NULL
);



CREATE SEQUENCE public.secuencia_folio
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



CREATE TABLE public.servicio (
    id uuid NOT NULL,
    nombre character varying(255) NOT NULL,
    descripcion text NOT NULL,
    area_id uuid NOT NULL,
    categoria_id uuid NOT NULL,
    plazo_dias_habiles integer NOT NULL,
    version_vigente integer NOT NULL,
    activo boolean NOT NULL,
    CONSTRAINT servicio_plazo_dias_habiles_check CHECK ((plazo_dias_habiles > 0))
);



CREATE TABLE public.solicitud (
    id uuid NOT NULL,
    folio character varying(255) NOT NULL,
    estudiante_id uuid NOT NULL,
    servicio_id uuid NOT NULL,
    formulario_version integer NOT NULL,
    datos jsonb NOT NULL,
    estado character varying(30) NOT NULL,
    responsable_id uuid,
    vence_en timestamp with time zone NOT NULL,
    creada_en timestamp with time zone NOT NULL,
    cerrada_en timestamp with time zone,
    version integer NOT NULL,
    CONSTRAINT solicitud_estado_check CHECK (((estado)::text = ANY ((ARRAY['REGISTRADA'::character varying, 'SIN_RESPUESTA'::character varying, 'EN_ATENCION'::character varying, 'DEMORADO'::character varying, 'ATENDIDA'::character varying, 'RECHAZADA'::character varying, 'CANCELADA'::character varying])::text[])))
);



CREATE TABLE public.usuario (
    id uuid NOT NULL,
    correo character varying(255) NOT NULL,
    nombre character varying(255) NOT NULL,
    contrasena character varying(255) NOT NULL,
    activo boolean NOT NULL
);



CREATE TABLE public.usuario_rol (
    usuario_id uuid NOT NULL,
    rol character varying(30) NOT NULL,
    CONSTRAINT usuario_rol_rol_check CHECK (((rol)::text = ANY ((ARRAY['ESTUDIANTE'::character varying, 'RESPONSABLE'::character varying, 'ADMINISTRADOR'::character varying])::text[])))
);



ALTER TABLE ONLY public.adjunto
    ADD CONSTRAINT adjunto_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.area
    ADD CONSTRAINT area_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.dia_inhabil
    ADD CONSTRAINT dia_inhabil_pkey PRIMARY KEY (fecha);



ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);



ALTER TABLE ONLY public.formulario_servicio
    ADD CONSTRAINT formulario_servicio_pkey PRIMARY KEY (servicio_id, version);



ALTER TABLE ONLY public.historial
    ADD CONSTRAINT historial_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.parametro
    ADD CONSTRAINT parametro_pkey PRIMARY KEY (clave);



ALTER TABLE ONLY public.responsable_area
    ADD CONSTRAINT responsable_area_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.responsable_area
    ADD CONSTRAINT responsable_area_usuario_id_area_id_key UNIQUE (usuario_id, area_id);



ALTER TABLE ONLY public.servicio
    ADD CONSTRAINT servicio_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_folio_key UNIQUE (folio);



ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_correo_key UNIQUE (correo);



ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.usuario_rol
    ADD CONSTRAINT usuario_rol_pkey PRIMARY KEY (usuario_id, rol);



CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);



CREATE UNIQUE INDEX solicitud_abierta ON public.solicitud USING btree (estudiante_id, servicio_id) WHERE ((estado)::text = ANY ((ARRAY['REGISTRADA'::character varying, 'SIN_RESPUESTA'::character varying, 'EN_ATENCION'::character varying, 'DEMORADO'::character varying])::text[]));



CREATE TRIGGER formulario_inmutable BEFORE DELETE OR UPDATE ON public.formulario_servicio FOR EACH ROW EXECUTE FUNCTION public.rechazar_formulario();



CREATE TRIGGER formulario_no_truncar BEFORE TRUNCATE ON public.formulario_servicio FOR EACH STATEMENT EXECUTE FUNCTION public.rechazar_formulario();



CREATE TRIGGER historial_inmutable BEFORE DELETE OR UPDATE ON public.historial FOR EACH ROW EXECUTE FUNCTION public.rechazar_historial();



CREATE TRIGGER historial_no_truncar BEFORE TRUNCATE ON public.historial FOR EACH STATEMENT EXECUTE FUNCTION public.rechazar_historial();



ALTER TABLE ONLY public.adjunto
    ADD CONSTRAINT adjunto_solicitud_id_fkey FOREIGN KEY (solicitud_id) REFERENCES public.solicitud(id);



ALTER TABLE ONLY public.formulario_servicio
    ADD CONSTRAINT formulario_servicio_servicio_id_fkey FOREIGN KEY (servicio_id) REFERENCES public.servicio(id);



ALTER TABLE ONLY public.servicio
    ADD CONSTRAINT formulario_vigente FOREIGN KEY (id, version_vigente) REFERENCES public.formulario_servicio(servicio_id, version) DEFERRABLE INITIALLY DEFERRED;



ALTER TABLE ONLY public.historial
    ADD CONSTRAINT historial_autor_id_fkey FOREIGN KEY (autor_id) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.historial
    ADD CONSTRAINT historial_solicitud_id_fkey FOREIGN KEY (solicitud_id) REFERENCES public.solicitud(id);



ALTER TABLE ONLY public.responsable_area
    ADD CONSTRAINT responsable_area_area_id_fkey FOREIGN KEY (area_id) REFERENCES public.area(id);



ALTER TABLE ONLY public.responsable_area
    ADD CONSTRAINT responsable_area_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.servicio
    ADD CONSTRAINT servicio_area_id_fkey FOREIGN KEY (area_id) REFERENCES public.area(id);



ALTER TABLE ONLY public.servicio
    ADD CONSTRAINT servicio_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categoria(id);



ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_estudiante_id_fkey FOREIGN KEY (estudiante_id) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_responsable_id_fkey FOREIGN KEY (responsable_id) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.solicitud
    ADD CONSTRAINT solicitud_servicio_id_formulario_version_fkey FOREIGN KEY (servicio_id, formulario_version) REFERENCES public.formulario_servicio(servicio_id, version);



ALTER TABLE ONLY public.usuario_rol
    ADD CONSTRAINT usuario_rol_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuario(id);




