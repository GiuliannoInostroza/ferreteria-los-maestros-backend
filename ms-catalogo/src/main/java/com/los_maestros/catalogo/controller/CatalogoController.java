package com.los_maestros.catalogo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.los_maestros.catalogo.dto.CatalogoResponse;
import com.los_maestros.catalogo.service.CatalogoService;

@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogoController {
    private final CatalogoService service;

    public CatalogoController(CatalogoService service) { this.service = service; }

    @GetMapping public CatalogoResponse obtener() { return service.obtener(); }
}
