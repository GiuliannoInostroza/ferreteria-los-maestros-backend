
package com.los_maestros.contenido.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.los_maestros.contenido.model.Blog;
import com.los_maestros.contenido.service.BlogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService service;

    public BlogController(BlogService service) {
        this.service = service;
    }

    @GetMapping
    public List<Blog> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Blog buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Blog> crear(@Valid @RequestBody Blog blog) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(blog));
    }

    @PutMapping("/{id}")
    public Blog actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Blog blog) {
        return service.actualizar(id, blog);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
