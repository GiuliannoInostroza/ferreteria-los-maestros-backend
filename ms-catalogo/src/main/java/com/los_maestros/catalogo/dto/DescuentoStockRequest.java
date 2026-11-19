package com.los_maestros.catalogo.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record DescuentoStockRequest(
    @Valid @NotEmpty(message = "Debe indicar al menos un producto") List<ItemStock> items
) {}
