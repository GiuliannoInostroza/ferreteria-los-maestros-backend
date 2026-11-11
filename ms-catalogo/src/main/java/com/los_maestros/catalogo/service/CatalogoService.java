package com.los_maestros.catalogo.service;

import org.springframework.stereotype.Service;

import com.los_maestros.catalogo.dto.CatalogoResponse;

@Service
public class CatalogoService {
    private final ProductoService productos;
    private final CategoriaService categorias;
    private final OfertaService ofertas;

    public CatalogoService(ProductoService productos, CategoriaService categorias,
            OfertaService ofertas) {
        this.productos = productos;
        this.categorias = categorias;
        this.ofertas = ofertas;
    }

    public CatalogoResponse obtener() {
        return new CatalogoResponse(productos.listar(), categorias.listar(), ofertas.listarActivas());
    }
}
