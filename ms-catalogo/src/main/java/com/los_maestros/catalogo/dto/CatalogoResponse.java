package com.los_maestros.catalogo.dto;

import java.util.List;

import com.los_maestros.catalogo.model.Categoria;
import com.los_maestros.catalogo.model.Oferta;
import com.los_maestros.catalogo.model.Producto;

public record CatalogoResponse(
    List<Producto> productos,
    List<Categoria> categorias,
    List<Oferta> ofertas
) {}
