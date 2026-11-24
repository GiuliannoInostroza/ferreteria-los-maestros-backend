package com.los_maestros.pedidos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.los_maestros.pedidos.dto.PedidoRequest;
import com.los_maestros.pedidos.model.Pedido;
import com.los_maestros.pedidos.model.DetallePedido;
import com.los_maestros.pedidos.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) { this.service = service; }

    @GetMapping
    public List<Pedido> listar() {
        return service.listar();
    }

    @GetMapping("/mios")
    public List<Pedido> mios() {
        return service.listarPorCliente(correoSesion());
    }

    @GetMapping("/{id}")
    public Pedido buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest datos,
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(datos, correoSesion(), authorization));
    }

    @PutMapping("/{id}")
    public Pedido actualizar(@PathVariable Long id,
            @Valid @RequestBody Pedido pedido) {
        return service.actualizar(id, pedido);
    }

    @GetMapping("/{id}/detalles")
    public List<DetallePedido> listarDetallesPorPedido(@PathVariable Long id) {
        return service.listarDetallesPorPedido(id);
    }

    @GetMapping("/reportes/detalles")
    public List<DetallePedido> listarDetallesReporte() {
        return service.listarDetalles();
    }

    private String correoSesion() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }
}
