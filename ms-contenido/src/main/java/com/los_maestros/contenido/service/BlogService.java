
package com.los_maestros.contenido.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.contenido.exception.RecursoNoEncontradoException;
import com.los_maestros.contenido.model.Blog;
import com.los_maestros.contenido.repository.BlogRepository;

@Service
public class BlogService {

    private final BlogRepository repository;

    public BlogService(BlogRepository repository) {
        this.repository = repository;
    }

    public List<Blog> listar() {
        return repository.findAll();
    }

    public Blog buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Blog no encontrado: " + id));
    }

    public Blog crear(Blog blog) {
        blog.setId(null);
        return repository.save(blog);
    }

    public Blog actualizar(Long id, Blog datos) {
        Blog actual = buscar(id);

        actual.setCategoria(datos.getCategoria());
        actual.setFecha(datos.getFecha());
        actual.setTitulo(datos.getTitulo());
        actual.setResumen(datos.getResumen());
        actual.setImagen(datos.getImagen());
        actual.setContenido(datos.getContenido());

        return repository.save(actual);
    }

    public void eliminar(Long id) {
        Blog actual = buscar(id);
        repository.delete(actual);
    }
}
