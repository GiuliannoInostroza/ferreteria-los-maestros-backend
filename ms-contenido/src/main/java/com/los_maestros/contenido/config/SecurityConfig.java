
package com.los_maestros.contenido.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.los_maestros.contenido.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {

        return http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

    .requestMatchers(
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/error"
    ).permitAll()
        // Blogs: lectura pública
        .requestMatchers(HttpMethod.GET, "/api/blogs", "/api/blogs/**")
            .permitAll()

        // Blogs: escritura solo Administrador
        .requestMatchers(HttpMethod.POST, "/api/blogs")
            .hasRole("Administrador")
        .requestMatchers(HttpMethod.PUT, "/api/blogs/**")
            .hasRole("Administrador")
        .requestMatchers(HttpMethod.DELETE, "/api/blogs/**")
            .hasRole("Administrador")

        // Contacto: cualquier visitante puede enviar
        .requestMatchers(HttpMethod.POST, "/api/contactos")
            .permitAll()

        // Contacto: solo Administrador puede consultar
        .requestMatchers(HttpMethod.GET, "/api/contactos", "/api/contactos/**")
            .hasRole("Administrador")

        // Contacto: solo Administrador puede eliminar
        .requestMatchers(HttpMethod.DELETE, "/api/contactos/**")
            .hasRole("Administrador")

        .anyRequest().denyAll()
    )

            .exceptionHandling(e -> e.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin}") String allowedOrigin) {

        CorsConfiguration c = new CorsConfiguration();

        c.setAllowedOrigins(List.of(allowedOrigin));
        c.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );
        c.setAllowedHeaders(
            List.of("Content-Type", "Authorization")
        );

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", c);

        return source;
    }
}
