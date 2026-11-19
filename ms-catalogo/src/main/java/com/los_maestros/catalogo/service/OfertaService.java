package com.los_maestros.catalogo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.catalogo.exception.RecursoNoEncontradoException;
import com.los_maestros.catalogo.exception.ReglaNegocioException;
import com.los_maestros.catalogo.model.Oferta;
import com.los_maestros.catalogo.repository.OfertaRepository;
import com.los_maestros.catalogo.repository.ProductoRepository;

@Service
public class OfertaService {
    private final OfertaRepository repository;
    private final ProductoRepository productos;

    public OfertaService(OfertaRepository repository, ProductoRepository productos) {
        this.repository = repository;
        this.productos = productos;
    }

    public List<Oferta> listarActivas() {
        List<Oferta> activas = new ArrayList<>();
        for (Oferta oferta : repository.findAll()) {
            if (Boolean.TRUE.equals(oferta.getActiva())) {
                activas.add(oferta);
            }
        }
        return activas;
    }

    public Oferta buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Oferta no encontrada: " + id));
    }

    public Oferta guardar(Oferta oferta) {
        productos.findById(oferta.getProductoId()).orElseThrow(() ->
            new RecursoNoEncontradoException("Producto no encontrado: " + oferta.getProductoId()));
        verificarPorcentaje(oferta.getPorcentaje());
        for (Oferta existente : repository.findByProductoId(oferta.getProductoId())) {
            if (Boolean.TRUE.equals(existente.getActiva())) {
                throw new ReglaNegocioException("El producto ya tiene una oferta activa");
            }
        }
        oferta.setId(null);
        oferta.setActiva(true);
        return repository.save(oferta);
    }

    public Oferta actualizar(Long id, Oferta datos) {
        Oferta actual = buscar(id);
        verificarPorcentaje(datos.getPorcentaje());
        actual.setPorcentaje(datos.getPorcentaje());
        actual.setActiva(datos.getActiva());
        return repository.save(actual);
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private void verificarPorcentaje(Integer porcentaje) {
        if (porcentaje > 90) {
            throw new ReglaNegocioException("El porcentaje no puede superar 90");
        }
    }
}
