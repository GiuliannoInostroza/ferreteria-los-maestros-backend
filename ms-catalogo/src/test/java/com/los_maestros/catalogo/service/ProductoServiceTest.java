package com.los_maestros.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.los_maestros.catalogo.dto.DescuentoStockRequest;
import com.los_maestros.catalogo.dto.ItemStock;
import com.los_maestros.catalogo.exception.ReglaNegocioException;
import com.los_maestros.catalogo.model.Producto;
import com.los_maestros.catalogo.repository.CategoriaRepository;
import com.los_maestros.catalogo.repository.OfertaRepository;
import com.los_maestros.catalogo.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock ProductoRepository repository;
    @Mock CategoriaRepository categorias;
    @Mock OfertaRepository ofertas;
    @InjectMocks ProductoService service;

    private Producto producto(int stock) {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Cemento");
        p.setStock(stock);
        return p;
    }

    @Test
    void descuentaElStockDeCadaProducto() {
        Producto cemento = producto(10);
        when(repository.findById(1L)).thenReturn(Optional.of(cemento));
        when(repository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        service.descontarStock(new DescuentoStockRequest(List.of(new ItemStock(1L, 3))));

        assertEquals(7, cemento.getStock().intValue());
    }

    @Test
    void noDejaElStockNegativoSiElMismoProductoVieneDosVeces() {
        Producto cemento = producto(5);
        when(repository.findById(1L)).thenReturn(Optional.of(cemento));
        when(repository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(ReglaNegocioException.class, () -> service.descontarStock(
            new DescuentoStockRequest(List.of(new ItemStock(1L, 4), new ItemStock(1L, 4)))));
    }
}
