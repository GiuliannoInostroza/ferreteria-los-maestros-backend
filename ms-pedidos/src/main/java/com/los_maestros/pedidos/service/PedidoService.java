package com.los_maestros.pedidos.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

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
    private final RestClient catalogo;

    public PedidoService(PedidoRepository repository, DetallePedidoRepository detalles,
            @Value("${app.catalogo.url:http://localhost:8091/api/v1/catalogo}") String urlCatalogo) {
        this.repository = repository;
        this.detalles = detalles;
        this.catalogo = RestClient.create(urlCatalogo);
    }

    public List<Pedido> listar() { return repository.findAll(); }

    public List<Pedido> listarPorCliente(String correo) {
        return repository.findByCorreoClienteIgnoreCase(correo);
    }

    public Pedido buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
            new RecursoNoEncontradoException("Pedido no encontrado: " + id));
    }

    // Las ofertas se aplican igual que en React, pero calculadas en el servidor.
    public record ProductoCatalogo(Long id, String codigo, String nombre, Integer precio, Integer stock) {}
    public record OfertaCatalogo(Long productoId, Integer porcentaje) {}
    public record RespuestaCatalogo(List<ProductoCatalogo> productos, List<OfertaCatalogo> ofertas) {}

    @Transactional
    public Pedido crear(PedidoRequest datos, String correo) {
        RespuestaCatalogo respuesta;
        try {
            respuesta = catalogo.get().retrieve().body(RespuestaCatalogo.class);
        } catch (RestClientException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar el catálogo", error);
        }
        if (respuesta == null || respuesta.productos() == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El catálogo no está disponible");
        }

        int total = 0;
        Set<String> codigos = new HashSet<>();
        List<DetallePedido> detallesNuevos = new java.util.ArrayList<>();

        for (DetallePedido recibido : datos.detalles()) {
            if (recibido.getCodigoProducto() == null || recibido.getCantidad() == null
                    || recibido.getCantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto o cantidad inválida");
            }
            String codigo = recibido.getCodigoProducto();
            if (!codigos.add(codigo.toLowerCase(java.util.Locale.ROOT))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto repetido: " + codigo);
            }

            ProductoCatalogo producto = respuesta.productos().stream()
                    .filter(p -> p.codigo() != null && p.codigo().equalsIgnoreCase(codigo))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Producto inexistente: " + codigo));

            if (producto.precio() == null || producto.stock() == null
                    || producto.stock() < recibido.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Precio o stock inválido para " + codigo);
            }

            int precio = producto.precio();
            if (respuesta.ofertas() != null) {
                for (OfertaCatalogo oferta : respuesta.ofertas()) {
                    if (producto.id().equals(oferta.productoId()) && oferta.porcentaje() != null) {
                        precio = (int) (((long) precio * (100 - oferta.porcentaje())) / 100);
                        break;
                    }
                }
            }
            try {
                total = Math.addExact(total, Math.multiplyExact(recibido.getCantidad(), precio));
            } catch (ArithmeticException error) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total del pedido demasiado grande");
            }
            // Nunca guardamos el id ni el precio que envió el navegador.
            DetallePedido nuevo = new DetallePedido();
            nuevo.setCodigoProducto(producto.codigo());
            nuevo.setNombreProducto(producto.nombre());
            nuevo.setCantidad(recibido.getCantidad());
            nuevo.setPrecioUnitario(precio);
            detallesNuevos.add(nuevo);
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

        for (DetallePedido detalle : detallesNuevos) {
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
