package com.los_maestros.pedidos.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.los_maestros.pedidos.service.CatalogoCliente.ProductoVigente;

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
    private final CatalogoCliente catalogo;

    public PedidoService(PedidoRepository repository, DetallePedidoRepository detalles,
            CatalogoCliente catalogo) {
        this.repository = repository;
        this.detalles = detalles;
        this.catalogo = catalogo;
    }

    public List<Pedido> listar() { return repository.findAll(); }

    public List<Pedido> listarPorCliente(String correo) {
        return repository.findByCorreoClienteIgnoreCase(correo);
    }

    public Pedido buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Pedido no encontrado: " + id));
    }

    public Pedido crear(PedidoRequest datos, String correo, String authorization) {
        Map<String, ProductoVigente> vigentes = catalogo.productosVigentes(authorization);
        int total = 0;
        for (DetallePedido detalle : datos.detalles()) {
            ProductoVigente producto = vigentes.get(detalle.getCodigoProducto());
            if (producto == null) {
                throw new RecursoNoEncontradoException(
                    "Producto no encontrado en el catálogo: " + detalle.getCodigoProducto());
            }
            detalle.setId(null);
            detalle.setNombreProducto(producto.nombre());
            detalle.setPrecioUnitario(producto.precio());
            total += detalle.getCantidad() * producto.precio();
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

    public List<DetallePedido> listarDetalles() {
        return detalles.findAll();
    }

    public Pedido actualizar(Long id, Pedido datos) {
        Pedido actual = buscar(id);
        actual.setEstado(datos.getEstado());
        return repository.save(actual);
    }
}
