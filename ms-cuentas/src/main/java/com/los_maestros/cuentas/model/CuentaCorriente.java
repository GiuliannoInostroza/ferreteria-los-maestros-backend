package com.los_maestros.cuentas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cuenta_corriente")
public class CuentaCorriente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El correo del cliente es obligatorio")
    private String correoCliente;

    @NotNull(message = "El límite de crédito es obligatorio")
    @Min(value = 0, message = "El límite de crédito no puede ser negativo")
    private Integer limite;

    private Integer saldo = 0;

    private Boolean activa = true;
}
