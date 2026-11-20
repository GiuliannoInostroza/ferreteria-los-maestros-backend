package com.los_maestros.cuentas.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.cuentas.dto.MovimientoRequest;
import com.los_maestros.cuentas.exception.RecursoNoEncontradoException;
import com.los_maestros.cuentas.exception.ReglaNegocioException;
import com.los_maestros.cuentas.model.CuentaCorriente;
import com.los_maestros.cuentas.model.Movimiento;
import com.los_maestros.cuentas.repository.CuentaCorrienteRepository;
import com.los_maestros.cuentas.repository.MovimientoRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaService {
    private final CuentaCorrienteRepository repository;
    private final MovimientoRepository movimientos;

    public CuentaService(CuentaCorrienteRepository repository, MovimientoRepository movimientos) {
        this.repository = repository;
        this.movimientos = movimientos;
    }

    public List<CuentaCorriente> listar() { return repository.findAll(); }

    public CuentaCorriente buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Cuenta corriente no encontrada: " + id));
    }

    public CuentaCorriente buscarPorCorreo(String correo) {
        List<CuentaCorriente> cuentas = repository.findByCorreoClienteIgnoreCase(correo);
        if (cuentas.isEmpty()) {
            throw new RecursoNoEncontradoException("No tienes una cuenta corriente");
        }
        return cuentas.get(0);
    }

    public CuentaCorriente guardar(CuentaCorriente cuenta) {
        if (!repository.findByCorreoClienteIgnoreCase(cuenta.getCorreoCliente()).isEmpty()) {
            throw new ReglaNegocioException("Ese cliente ya tiene una cuenta corriente");
        }
        cuenta.setId(null);
        cuenta.setSaldo(0);
        cuenta.setActiva(true);
        return repository.save(cuenta);
    }

    public CuentaCorriente actualizar(Long id, CuentaCorriente datos) {
        CuentaCorriente actual = buscar(id);
        if (datos.getLimite() < actual.getSaldo()) {
            throw new ReglaNegocioException("El límite no puede ser menor que el saldo pendiente");
        }
        actual.setLimite(datos.getLimite());
        actual.setActiva(datos.getActiva());
        return repository.save(actual);
    }

    public void eliminar(Long id) {
        CuentaCorriente actual = buscar(id);
        if (actual.getSaldo() != 0) {
            throw new ReglaNegocioException("No se puede eliminar: la cuenta tiene saldo pendiente");
        }
        for (Movimiento movimiento : movimientos.findByCuentaId(id)) {
            movimientos.delete(movimiento);
        }
        repository.delete(actual);
    }

    public List<Movimiento> listarMovimientos(Long cuentaId) {
        buscar(cuentaId);
        return movimientos.findByCuentaId(cuentaId);
    }

    @Transactional
    public Movimiento registrarCargo(Long cuentaId, MovimientoRequest datos) {
        CuentaCorriente cuenta = buscar(cuentaId);
        if (datos.pedidoId() != null &&
                movimientos.existsByCuentaIdAndTipoAndPedidoId(cuentaId, "Cargo", datos.pedidoId())) {
            throw new ReglaNegocioException("El pedido ya tiene un cargo en esta cuenta");
        }
        if (!Boolean.TRUE.equals(cuenta.getActiva())) {
            throw new ReglaNegocioException("La cuenta corriente está inactiva");
        }
        if (cuenta.getSaldo() + datos.monto() > cuenta.getLimite()) {
            throw new ReglaNegocioException("El cargo supera el límite de crédito");
        }
        cuenta.setSaldo(cuenta.getSaldo() + datos.monto());
        repository.save(cuenta);
        return guardarMovimiento(cuentaId, "Cargo", datos);
    }

    @Transactional
    public Movimiento registrarAbono(Long cuentaId, MovimientoRequest datos) {
        CuentaCorriente cuenta = buscar(cuentaId);
        if (datos.monto() > cuenta.getSaldo()) {
            throw new ReglaNegocioException("El abono supera el saldo pendiente");
        }
        cuenta.setSaldo(cuenta.getSaldo() - datos.monto());
        repository.save(cuenta);
        return guardarMovimiento(cuentaId, "Abono", datos);
    }

    private Movimiento guardarMovimiento(Long cuentaId, String tipo, MovimientoRequest datos) {
        Movimiento movimiento = new Movimiento();
        movimiento.setCuentaId(cuentaId);
        movimiento.setTipo(tipo);
        movimiento.setMonto(datos.monto());
        movimiento.setDescripcion(datos.descripcion());
        movimiento.setPedidoId(datos.pedidoId());
        movimiento.setFecha(LocalDate.now());
        return movimientos.save(movimiento);
    }
}
