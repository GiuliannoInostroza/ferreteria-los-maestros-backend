package com.los_maestros.usuarios.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.los_maestros.usuarios.dto.UsuarioRequest;
import com.los_maestros.usuarios.exception.CorreoDuplicadoException;
import com.los_maestros.usuarios.exception.RecursoNoEncontradoException;
import com.los_maestros.usuarios.model.Usuario;
import com.los_maestros.usuarios.repository.UsuarioRepository;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public List<Usuario> listar() { return repository.findAll(); }

    public List<Usuario> listarPorRol(String rol) { return repository.findByRolIgnoreCase(rol); }

    public Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    public Usuario guardar(UsuarioRequest datos) {
        if (repository.findByCorreoIgnoreCase(datos.correo()).isPresent()) {
            throw new CorreoDuplicadoException("Ya existe una cuenta con ese correo");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(datos.nombre());
        usuario.setCorreo(datos.correo());
        usuario.setPassword(encoder.encode(datos.password()));
        usuario.setRol(datos.rol());
        usuario.setActivo(true);
        return repository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datos) {
        Usuario actual = buscar(id);
        actual.setNombre(datos.getNombre());
        actual.setCorreo(datos.getCorreo());
        actual.setRol(datos.getRol());
        actual.setActivo(datos.getActivo());
        return repository.save(actual);
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }
}
