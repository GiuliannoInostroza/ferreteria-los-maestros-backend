package com.los_maestros.cuentas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovimientoRequest(
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 1, message = "El monto debe ser mayor que 0") Integer monto,
    String descripcion,
    Long pedidoId
) {}
