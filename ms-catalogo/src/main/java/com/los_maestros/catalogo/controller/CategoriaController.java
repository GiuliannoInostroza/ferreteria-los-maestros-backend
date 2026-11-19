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

import com.los_maestros.catalogo.model.Categoria;
import com.los_maestros.catalogo.service.CategoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {
    private final CategoriaService service;

    public CategoriaController(CategoriaService service) { this.service = service; }

    @GetMapping public List<Categoria> listar() { return service.listar(); }

    @GetMapping("/{id}") public Categoria buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria categoria) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(categoria));
    }

    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable Long id,
        @Valid @RequestBody Categoria categoria) {
        return service.actualizar(id, categoria);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
