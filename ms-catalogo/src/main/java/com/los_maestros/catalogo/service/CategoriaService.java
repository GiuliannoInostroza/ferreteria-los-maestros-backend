package com.los_maestros.catalogo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.catalogo.exception.RecursoNoEncontradoException;
import com.los_maestros.catalogo.exception.ReglaNegocioException;
import com.los_maestros.catalogo.model.Categoria;
import com.los_maestros.catalogo.repository.CategoriaRepository;
import com.los_maestros.catalogo.repository.ProductoRepository;

@Service
public class CategoriaService {
    private final CategoriaRepository repository;
    private final ProductoRepository productos;

    public CategoriaService(CategoriaRepository repository, ProductoRepository productos) {
        this.repository = repository;
        this.productos = productos;
    }

    public List<Categoria> listar() { return repository.findAll(); }

    public Categoria buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Categoría no encontrada: " + id));
    }

    public Categoria guardar(Categoria categoria) {
        if (!repository.findByNombreIgnoreCase(categoria.getNombre()).isEmpty()) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
        }
        categoria.setId(null);
        return repository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria datos) {
        Categoria actual = buscar(id);
        for (Categoria otra : repository.findByNombreIgnoreCase(datos.getNombre())) {
            if (!otra.getId().equals(id)) {
                throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
            }
        }
        actual.setNombre(datos.getNombre());
        actual.setDescripcion(datos.getDescripcion());
        return repository.save(actual);
    }

    public void eliminar(Long id) {
        Categoria actual = buscar(id);
        if (!productos.findByCategoriaId(id).isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar: la categoría tiene productos");
        }
        repository.delete(actual);
    }
}
