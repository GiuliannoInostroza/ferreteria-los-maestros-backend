package com.los_maestros.usuarios.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.los_maestros.usuarios.model.Usuario;
import com.los_maestros.usuarios.repository.UsuarioRepository;

@Configuration
public class DatosIniciales {
    @Value("${app.admin.correo}") private String correo;
    @Value("${app.admin.password}") private String password;

    @Bean CommandLineRunner usuarioInicial(UsuarioRepository repo,
            PasswordEncoder encoder) {
        return args -> {
            if (repo.findByCorreoIgnoreCase(correo).isEmpty()) {
                Usuario u = new Usuario();
                u.setNombre("Administrador");
                u.setCorreo(correo);
                u.setPassword(encoder.encode(password));
                u.setRol("Administrador");
                u.setActivo(true);
                repo.save(u);
            }
        };
    }
}
