
package com.los_maestros.contenido.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "blog")
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El resumen es obligatorio")
    @Column(length = 1000)
    private String resumen;

    private String imagen;

    @NotBlank(message = "El contenido es obligatorio")
    @Column(columnDefinition = "TEXT")
    private String contenido;
}
