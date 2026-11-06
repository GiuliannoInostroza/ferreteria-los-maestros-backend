package com.los_maestros.pedidos.dto;

import java.util.List;

import com.los_maestros.pedidos.model.DetallePedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record PedidoRequest(
    @NotBlank(message = "El tipo de entrega es obligatorio") String tipoEntrega,
    String direccionDespacho,
    @NotBlank(message = "El medio de pago es obligatorio") String medioPago,
    @Valid @NotEmpty(message = "El pedido debe tener al menos un producto") List<DetallePedido> detalles
) {}
