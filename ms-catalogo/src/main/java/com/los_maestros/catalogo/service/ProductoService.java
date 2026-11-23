package com.los_maestros.catalogo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.catalogo.dto.DescuentoStockRequest;
import com.los_maestros.catalogo.dto.ItemStock;
import com.los_maestros.catalogo.exception.RecursoNoEncontradoException;
import com.los_maestros.catalogo.exception.ReglaNegocioException;
import com.los_maestros.catalogo.model.Oferta;
import com.los_maestros.catalogo.model.Producto;
import com.los_maestros.catalogo.repository.CategoriaRepository;
import com.los_maestros.catalogo.repository.OfertaRepository;
import com.los_maestros.catalogo.repository.ProductoRepository;

@Service
public class ProductoService {
    private final ProductoRepository repository;
    private final CategoriaRepository categorias;
    private final OfertaRepository ofertas;

    public ProductoService(ProductoRepository repository, CategoriaRepository categorias,
            OfertaRepository ofertas) {
        this.repository = repository;
        this.categorias = categorias;
        this.ofertas = ofertas;
    }

    public List<Producto> listar() { return repository.findAll(); }

    public Producto buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Producto no encontrado: " + id));
    }

    public List<Producto> alertas() {
        List<Producto> conStockCritico = new ArrayList<>();
        for (Producto producto : repository.findAll()) {
            if (producto.getStock() <= producto.getStockCritico()) {
                conStockCritico.add(producto);
            }
        }
        return conStockCritico;
    }

    public Producto guardar(Producto producto) {
        if (!repository.findByCodigoIgnoreCase(producto.getCodigo()).isEmpty()) {
            throw new ReglaNegocioException("Ya existe un producto con ese código");
        }
        verificarCategoria(producto.getCategoriaId());
        producto.setId(null);
        return repository.save(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto actual = buscar(id);
        for (Producto otro : repository.findByCodigoIgnoreCase(datos.getCodigo())) {
            if (!otro.getId().equals(id)) {
                throw new ReglaNegocioException("Ya existe un producto con ese código");
            }
        }
        verificarCategoria(datos.getCategoriaId());
        actual.setCodigo(datos.getCodigo());
        actual.setNombre(datos.getNombre());
        actual.setMarca(datos.getMarca());
        actual.setDescripcion(datos.getDescripcion());
        actual.setPrecio(datos.getPrecio());
        actual.setStock(datos.getStock());
        actual.setStockCritico(datos.getStockCritico());
        actual.setDestacado(datos.getDestacado());
        actual.setCategoriaId(datos.getCategoriaId());
        return repository.save(actual);
    }

    public Producto cambiarStock(Long id, Integer stock) {
        Producto actual = buscar(id);
        actual.setStock(stock);
        return repository.save(actual);
    }

    public void eliminar(Long id) {
        Producto actual = buscar(id);
        for (Oferta oferta : ofertas.findByProductoId(id)) {
            ofertas.delete(oferta);
        }
        repository.delete(actual);
    }

    public List<Producto> descontarStock(DescuentoStockRequest datos) {
        for (ItemStock item : datos.items()) {
            Producto producto = buscar(item.productoId());
            if (producto.getStock() < item.cantidad()) {
                throw new ReglaNegocioException("Stock insuficiente de " + producto.getNombre());
            }
        }
        List<Producto> actualizados = new ArrayList<>();
        for (ItemStock item : datos.items()) {
            Producto producto = buscar(item.productoId());
            producto.setStock(producto.getStock() - item.cantidad());
            actualizados.add(repository.save(producto));
        }
        return actualizados;
    }

    private void verificarCategoria(Long categoriaId) {
        categorias.findById(categoriaId).orElseThrow(() ->
            new RecursoNoEncontradoException("Categoría no encontrada: " + categoriaId));
    }
}
