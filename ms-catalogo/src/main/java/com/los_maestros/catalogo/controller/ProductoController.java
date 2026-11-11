package com.los_maestros.catalogo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.los_maestros.catalogo.dto.DescuentoStockRequest;
import com.los_maestros.catalogo.dto.StockRequest;
import com.los_maestros.catalogo.model.Producto;
import com.los_maestros.catalogo.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
    private final ProductoService service;

    public ProductoController(ProductoService service) { this.service = service; }

    @GetMapping public List<Producto> listar() { return service.listar(); }

    @GetMapping("/alertas") public List<Producto> alertas() { return service.alertas(); }

    @GetMapping("/{id}") public Producto buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(producto));
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id,
        @Valid @RequestBody Producto producto) {
        return service.actualizar(id, producto);
    }

    @PutMapping("/{id}/stock")
    public Producto cambiarStock(@PathVariable Long id,
        @Valid @RequestBody StockRequest datos) {
        return service.cambiarStock(id, datos.stock());
    }

    @PostMapping("/descontar-stock")
    public List<Producto> descontarStock(@Valid @RequestBody DescuentoStockRequest datos) {
        return service.descontarStock(datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
