package com.los_maestros.usuarios.dto;

import com.los_maestros.usuarios.model.Usuario;

public record LoginResponse(String token, Usuario usuario) {}