
package com.los_maestros.contenido.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.los_maestros.contenido.model.Contacto;
import com.los_maestros.contenido.service.ContactoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contactos")
public class ContactoController {

    private final ContactoService service;

    public ContactoController(ContactoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Contacto> crear(
            @Valid @RequestBody Contacto contacto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(contacto));
    }

    @GetMapping
    public List<Contacto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Contacto buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
