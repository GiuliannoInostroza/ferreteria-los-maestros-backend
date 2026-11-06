package com.los_maestros.pedidos.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String correoCliente;

    @NotBlank(message = "El tipo de entrega es obligatorio")
    private String tipoEntrega;

    private String direccionDespacho;

    @NotBlank(message = "El medio de pago es obligatorio")
    private String medioPago;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    private Integer total;

    private LocalDate fecha;
}
