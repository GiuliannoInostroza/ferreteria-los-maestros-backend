package com.los_maestros.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.los_maestros.catalogo.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCodigoIgnoreCase(String codigo);

    List<Producto> findByCategoriaId(Long categoriaId);
}
