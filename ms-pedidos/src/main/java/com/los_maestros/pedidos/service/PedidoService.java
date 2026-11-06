package com.los_maestros.pedidos.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.los_maestros.pedidos.dto.PedidoRequest;
import com.los_maestros.pedidos.exception.RecursoNoEncontradoException;
import com.los_maestros.pedidos.model.DetallePedido;
import com.los_maestros.pedidos.model.Pedido;
import com.los_maestros.pedidos.repository.DetallePedidoRepository;
import com.los_maestros.pedidos.repository.PedidoRepository;

@Service
public class PedidoService {
    private final PedidoRepository repository;
    private final DetallePedidoRepository detalles;

    public PedidoService(PedidoRepository repository, DetallePedidoRepository detalles) {
        this.repository = repository;
        this.detalles = detalles;
    }

    public List<Pedido> listar() { return repository.findAll(); }

    public List<Pedido> listarPorCliente(String correo) {
        return repository.findByCorreoClienteIgnoreCase(correo);
    }

    public Pedido buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Pedido no encontrado: " + id));
    }

    public Pedido crear(PedidoRequest datos, String correo) {
        int total = 0;
        for (DetallePedido detalle : datos.detalles()) {
            total += detalle.getCantidad() * detalle.getPrecioUnitario();
        }
        Pedido pedido = new Pedido();
        pedido.setCorreoCliente(correo);
        pedido.setTipoEntrega(datos.tipoEntrega());
        pedido.setDireccionDespacho(datos.direccionDespacho());
        pedido.setMedioPago(datos.medioPago());
        pedido.setEstado("Pendiente");
        pedido.setTotal(total);
        pedido.setFecha(LocalDate.now());
        Pedido guardado = repository.save(pedido);
        for (DetallePedido detalle : datos.detalles()) {
            detalle.setPedidoId(guardado.getId());
            detalles.save(detalle);
        }
        return guardado;
    }

    public Pedido actualizar(Long id, Pedido datos) {
        Pedido actual = buscar(id);
        actual.setEstado(datos.getEstado());
        return repository.save(actual);
    }
}
