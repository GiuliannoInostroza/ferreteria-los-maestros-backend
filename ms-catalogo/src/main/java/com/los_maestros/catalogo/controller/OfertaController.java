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

import com.los_maestros.catalogo.model.Oferta;
import com.los_maestros.catalogo.service.OfertaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ofertas")
public class OfertaController {
    private final OfertaService service;

    public OfertaController(OfertaService service) { this.service = service; }

    @GetMapping public List<Oferta> listar() { return service.listarActivas(); }

    @GetMapping("/{id}") public Oferta buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<Oferta> crear(@Valid @RequestBody Oferta oferta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(oferta));
    }

    @PutMapping("/{id}")
    public Oferta actualizar(@PathVariable Long id, @Valid @RequestBody Oferta oferta) {
        return service.actualizar(id, oferta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
