package mx.utch.sigsu.notificaciones.model;

import java.util.List;
public record Aviso(List<String> destinatarios, String asunto, String texto) {}