package com.los_maestros.usuarios.dto;

public record UsuarioResponse(Long id, String nombre, String correo, String rol, Boolean activo) {}
