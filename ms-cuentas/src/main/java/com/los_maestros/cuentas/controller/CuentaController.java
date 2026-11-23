package com.los_maestros.cuentas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.los_maestros.cuentas.dto.MovimientoRequest;
import com.los_maestros.cuentas.model.CuentaCorriente;
import com.los_maestros.cuentas.model.Movimiento;
import com.los_maestros.cuentas.service.CuentaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {
    private final CuentaService service;

    public CuentaController(CuentaService service) { this.service = service; }

    @GetMapping public List<CuentaCorriente> listar() { return service.listar(); }

    @GetMapping("/mia")
    public CuentaCorriente miCuenta() {
        return service.buscarPorCorreo(correoSesion());
    }

    @GetMapping("/mia/movimientos")
    public List<Movimiento> misMovimientos() {
        return service.listarMovimientos(service.buscarPorCorreo(correoSesion()).getId());
    }

    @PostMapping("/mia/cargos")
    public ResponseEntity<Movimiento> cargarAMiCuenta(@Valid @RequestBody MovimientoRequest datos) {
        Long cuentaId = service.buscarPorCorreo(correoSesion()).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarCargo(cuentaId, datos));
    }

    @GetMapping("/{id}") public CuentaCorriente buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<CuentaCorriente> crear(@Valid @RequestBody CuentaCorriente cuenta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(cuenta));
    }

    @PutMapping("/{id}")
    public CuentaCorriente actualizar(@PathVariable Long id,
        @Valid @RequestBody CuentaCorriente cuenta) {
        return service.actualizar(id, cuenta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }

    @GetMapping("/{id}/movimientos")
    public List<Movimiento> movimientos(@PathVariable Long id) {
        return service.listarMovimientos(id);
    }

    @PostMapping("/{id}/abonos")
    public ResponseEntity<Movimiento> abonar(@PathVariable Long id,
        @Valid @RequestBody MovimientoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarAbono(id, datos));
    }

    private String correoSesion() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
