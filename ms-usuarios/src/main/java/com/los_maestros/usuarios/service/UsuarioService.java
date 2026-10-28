package com.los_maestros.usuarios.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.usuarios.exception.RecursoNoEncontradoException;
import com.los_maestros.usuarios.model.Usuario;
import com.los_maestros.usuarios.repository.UsuarioRepository;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> listar() { return repository.findAll(); }

    public List<Usuario> listarPorRol(String rol) { return repository.findByRolIgnoreCase(rol); }

    public Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    public Usuario guardar(Usuario usuario) {
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
