package com.los_maestros.usuarios.controller;

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

import com.los_maestros.usuarios.model.Modelo;
import com.los_maestros.usuarios.service.ModeloService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/modelos")
public class ModeloController {
private final ModeloService service;
public ModeloController(ModeloService service) { this.service = service; }
@GetMapping public List<Modelo> listar() { return service.listar(); }
@GetMapping("/{id}") public Modelo buscar(@PathVariable Long id) { return
service.buscar(id); }
@PostMapping
public ResponseEntity<Modelo> crear(@Valid @RequestBody Modelo modelo) {
return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(modelo));
}
@PutMapping("/{id}")
public Modelo actualizar(@PathVariable Long id,
@Valid @RequestBody Modelo modelo) {
return service.actualizar(id, modelo);
}
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
