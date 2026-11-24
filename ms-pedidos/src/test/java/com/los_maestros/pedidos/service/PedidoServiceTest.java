package com.los_maestros.pedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.los_maestros.pedidos.dto.PedidoRequest;
import com.los_maestros.pedidos.exception.RecursoNoEncontradoException;
import com.los_maestros.pedidos.model.DetallePedido;
import com.los_maestros.pedidos.model.Pedido;
import com.los_maestros.pedidos.repository.DetallePedidoRepository;
import com.los_maestros.pedidos.repository.PedidoRepository;
import com.los_maestros.pedidos.service.CatalogoCliente.ProductoVigente;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {
    @Mock PedidoRepository repository;
    @Mock DetallePedidoRepository detalles;
    @Mock CatalogoCliente catalogo;
    @InjectMocks PedidoService service;

    private DetallePedido detalle(String codigo, int cantidad, int precioEnviado) {
        DetallePedido d = new DetallePedido();
        d.setCodigoProducto(codigo);
        d.setNombreProducto("Nombre enviado por el front");
        d.setCantidad(cantidad);
        d.setPrecioUnitario(precioEnviado);
        return d;
    }

    @Test
    void usaElPrecioDelCatalogoYNoElQueEnviaElFront() {
        when(catalogo.productosVigentes("Bearer token")).thenReturn(
            Map.of("MC001", new ProductoVigente("Cemento Polpaico gris 25 kg", 5391)));
        when(repository.save(any(Pedido.class))).thenAnswer(invocacion -> {
            Pedido p = invocacion.getArgument(0);
            p.setId(1L);
            return p;
        });
        DetallePedido d = detalle("MC001", 2, 1);

        Pedido pedido = service.crear(new PedidoRequest("Retiro en tienda", null, "Pago en tienda",
            List.of(d)), "cliente@gmail.com", "Bearer token");

        assertEquals(10782, pedido.getTotal().intValue());
        assertEquals(5391, d.getPrecioUnitario().intValue());
        assertEquals("Cemento Polpaico gris 25 kg", d.getNombreProducto());
        assertEquals("Pendiente", pedido.getEstado());
    }

    @Test
    void rechazaUnProductoQueNoEstaEnElCatalogo() {
        when(catalogo.productosVigentes("Bearer token")).thenReturn(Map.of());

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(
            new PedidoRequest("Retiro en tienda", null, "Pago en tienda", List.of(detalle("XX999", 1, 100))),
            "cliente@gmail.com", "Bearer token"));
        verify(repository, never()).save(any(Pedido.class));
    }

    @Test
    void calculaElPrecioDeOfertaIgualQueElFront() {
        assertEquals(5391, CatalogoCliente.precioConOferta(5990, 10));
        assertEquals(323, CatalogoCliente.precioConOferta(380, 15));
    }
}
