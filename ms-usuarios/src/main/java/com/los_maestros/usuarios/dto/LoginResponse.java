package com.los_maestros.usuarios.dto;

public record LoginResponse(String token, String correo, String rol, String nombre) {}
