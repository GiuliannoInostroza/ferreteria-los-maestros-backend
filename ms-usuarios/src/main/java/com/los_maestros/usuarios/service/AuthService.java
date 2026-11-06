package com.los_maestros.usuarios.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.los_maestros.usuarios.dto.LoginRequest;
import com.los_maestros.usuarios.dto.LoginResponse;
import com.los_maestros.usuarios.dto.RegistroRequest;
import com.los_maestros.usuarios.dto.UsuarioResponse;
import com.los_maestros.usuarios.exception.CorreoDuplicadoException;
import com.los_maestros.usuarios.model.Usuario;
import com.los_maestros.usuarios.repository.UsuarioRepository;
import com.los_maestros.usuarios.security.JwtService;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios,
            PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByCorreoIgnoreCase(request.correo())
            .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!usuario.getActivo() || !encoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return new LoginResponse(jwt.generar(usuario),
            usuario.getCorreo(), usuario.getRol(), usuario.getNombre());
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        if (usuarios.findByCorreoIgnoreCase(request.correo()).isPresent()) {
            throw new CorreoDuplicadoException("Ya existe una cuenta con ese correo");
        }
        Usuario u = new Usuario();
        u.setNombre(request.nombre());
        u.setCorreo(request.correo());
        u.setPassword(encoder.encode(request.password()));
        u.setRol("Cliente");
        u.setActivo(true);
        Usuario guardado = usuarios.save(u);
        return new UsuarioResponse(guardado.getId(), guardado.getNombre(),
            guardado.getCorreo(), guardado.getRol(), guardado.getActivo());
    }
}
