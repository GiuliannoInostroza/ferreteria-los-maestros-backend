
package com.los_maestros.contenido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contacto")
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    private String email;

    @NotBlank(message = "El asunto es obligatorio")
    @Pattern(
        regexp = "productos|pedido|cotizacion|otro",
        message = "El asunto no es válido"
    )
    private String asunto;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 20, max = 500,
          message = "El mensaje debe tener entre 20 y 500 caracteres")
    @Column(columnDefinition = "TEXT")
    private String mensaje;
}
