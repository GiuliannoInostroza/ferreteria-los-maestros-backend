# Guía para terminar ms-usuarios

La Guía 15 ya está aplicada en `ms-usuarios`: entidad `Usuario`, repositorio, servicio, controlador `/api/usuarios`, errores en JSON, Swagger y conexión a MySQL. Esta guía sigue desde ahí, con el mismo formato: qué archivo crear, el código y qué debería pasar.

Orden: 0 levantar, 1 rol como tabla, 2 contraseña con BCrypt, 3 login con JWT, 4 proteger rutas por rol, 5 desactivar en vez de borrar, 6 Postman, 7 conectar el frontend, 8 repetir en los otros microservicios.

## 0. Levantar lo que ya existe

Ejecutar SQL una sola vez (cliente MySQL o consola `mysql` con un usuario administrador):

```sql
CREATE DATABASE db_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'usuarios_app'@'localhost' IDENTIFIED BY 'CAMBIAR_CLAVE';
GRANT ALL PRIVILEGES ON db_usuarios.* TO 'usuarios_app'@'localhost';
FLUSH PRIVILEGES;
```

Levantar en Linux, dentro de `ms-usuarios` (el archivo `.env` con `DB_USER` y `DB_PASSWORD` no se sube a Git):

```bash
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

En Windows (PowerShell):

```powershell
$env:DB_USER="usuarios_app"
$env:DB_PASSWORD="CAMBIAR_CLAVE"
.\mvnw.cmd spring-boot:run
```

Resultado esperado: en la consola aparece `HikariPool-1 - Start completed` y `Started UsuariosApplication`. Swagger queda en http://localhost:8090/swagger-ui/index.html con los cinco endpoints.

Si aparece "Unable to find a single main class", hay clases viejas compiladas: ejecutar `./mvnw clean` y volver a levantar.

## 1. El rol como tabla (3FN)

Hoy `rol` es un texto dentro de `usuario`, y se puede escribir "Admin", "admin" o "Administrador". En el modelo el rol es su propia tabla y el usuario guarda solo su id.

Crear archivo: `model/Rol.java`

```java
package com.los_maestros.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;
}
```

Crear archivo: `repository/RolRepository.java`

```java
package com.los_maestros.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.los_maestros.usuarios.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
```

Modificar `model/Usuario.java`: reemplazar el campo `rol` de texto por la relación.

```java
@NotNull(message = "El rol es obligatorio")
@ManyToOne
@JoinColumn(name = "id_rol")
private Rol rol;
```

Explicación:
- `@ManyToOne`: muchos usuarios tienen un mismo rol.
- `@JoinColumn(name = "id_rol")`: la columna de `usuario` que guarda el id del rol (la clave foránea).
- `findByNombre`: Spring arma la consulta por el nombre del método, igual que `findByRolIgnoreCase`.

Cargar los roles una vez (SQL):

```sql
INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR'), ('VENDEDOR'), ('CLIENTE');
```

Ajustes que siguen:
- En `UsuarioRepository`, `findByRolIgnoreCase` pasa a `findByRolNombre(String nombre)`.
- En Swagger, el cuerpo del POST ahora lleva `"rol": { "id": 1 }`.
- Como `ddl-auto=update` no borra la columna vieja de texto, en desarrollo es más simple borrar la tabla `usuario` y dejar que Spring la cree de nuevo.

Resultado esperado: `SHOW TABLES` muestra `rol` y `usuario`, y `usuario` tiene la columna `id_rol`.

## 2. Contraseña con BCrypt

Nunca se guarda la contraseña: se guarda su hash. BCrypt genera un texto distinto cada vez, y luego solo puede comparar si una clave coincide con ese hash.

Agregar dependencia en `pom.xml` (incluye BCrypt y se usa también en el paso 4):

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Ojo: desde que se agrega esta dependencia, Spring bloquea todas las rutas hasta configurar el paso 4. Conviene hacer los pasos 2, 3 y 4 seguidos.

Agregar a `model/Usuario.java`:

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
@Column(name = "password_hash", nullable = false)
private String passwordHash;
```

`WRITE_ONLY` hace que el hash nunca salga en las respuestas JSON (importar `com.fasterxml.jackson.annotation.JsonProperty`).

Crear archivo: `config/SeguridadConfig.java` (aquí solo el encoder; el paso 4 lo completa)

```java
package com.los_maestros.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeguridadConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

Crear archivo: `dto/RegistroRequest.java`

```java
package com.los_maestros.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
    @NotBlank(message = "El correo es obligatorio") @Email String correo,
    @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, message = "Mínimo 8 caracteres") String clave
) {}
```

Agregar a `UsuarioService`:

```java
public Usuario registrar(RegistroRequest datos) {
    if (repository.existsByCorreo(datos.correo().toLowerCase())) {
        throw new CorreoDuplicadoException("Ya existe una cuenta con ese correo");
    }
    Rol cliente = rolRepository.findByNombre("CLIENTE")
        .orElseThrow(() -> new RecursoNoEncontradoException("Rol CLIENTE no existe"));
    Usuario nuevo = new Usuario();
    nuevo.setNombre(datos.nombre());
    nuevo.setCorreo(datos.correo().toLowerCase());
    nuevo.setRol(cliente);
    nuevo.setActivo(true);
    nuevo.setPasswordHash(encoder.encode(datos.clave()));
    return repository.save(nuevo);
}
```

Para esto:
- `UsuarioService` recibe por constructor también `RolRepository` y `PasswordEncoder` (igual que recibe el repositorio).
- `UsuarioRepository` suma `boolean existsByCorreo(String correo);`.
- Se crea `CorreoDuplicadoException` copiando `RecursoNoEncontradoException`, y en `GlobalExceptionHandler` se responde 409 para ella.

Resultado esperado: en la base, `password_hash` empieza con `$2a$` y nunca aparece la clave real.

## 3. Login con JWT

El JWT es un texto firmado que el backend entrega al iniciar sesión. Lleva adentro el id, el correo y el rol, y expira. El frontend lo manda en cada petición en el encabezado `Authorization: Bearer <token>`, y cada microservicio comprueba la firma con la misma clave secreta.

Agregar dependencias en `pom.xml`:

```xml
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.13.0</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.13.0</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.13.0</version>
  <scope>runtime</scope>
</dependency>
```

Agregar en `application.properties` (la clave viene del entorno, igual que `DB_PASSWORD`; debe tener al menos 32 caracteres):

```properties
jwt.secret=${JWT_SECRET}
jwt.minutos=60
```

Crear archivo: `security/JwtService.java`

```java
package com.los_maestros.usuarios.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.los_maestros.usuarios.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey clave;
    private final long minutos;

    public JwtService(@Value("${jwt.secret}") String secreto, @Value("${jwt.minutos}") long minutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.minutos = minutos;
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
            .subject(usuario.getCorreo())
            .claim("id", usuario.getId())
            .claim("rol", usuario.getRol().getNombre())
            .issuedAt(ahora)
            .expiration(new Date(ahora.getTime() + minutos * 60 * 1000))
            .signWith(clave)
            .compact();
    }

    public Claims leer(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
}
```

Crear `dto/LoginRequest.java` (`correo`, `clave`) y `dto/LoginResponse.java` (`token`, `nombre`, `rol`) como `record`, igual que `RegistroRequest`.

Agregar a `UsuarioService`:

```java
public LoginResponse login(LoginRequest datos) {
    Usuario usuario = repository.findByCorreo(datos.correo().toLowerCase())
        .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));
    if (!usuario.getActivo() || !encoder.matches(datos.clave(), usuario.getPasswordHash())) {
        throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
    }
    return new LoginResponse(jwtService.generar(usuario), usuario.getNombre(), usuario.getRol().getNombre());
}
```

Para esto:
- `UsuarioRepository` suma `Optional<Usuario> findByCorreo(String correo);`.
- `CredencialesInvalidasException` responde 401 en `GlobalExceptionHandler`.
- El mensaje es el mismo si falla el correo o la clave, para no revelar qué correos existen.

Crear archivo: `controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioService service;

    public AuthController(UsuarioService service) { this.service = service; }

    @PostMapping("/registro")
    public ResponseEntity<Usuario> registro(@Valid @RequestBody RegistroRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(datos));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        return service.login(datos);
    }
}
```

Resultado esperado: un POST a `/api/auth/login` con datos correctos responde 200 y un `token`. Si se pega en https://jwt.io se ven `id`, `rol` y la fecha de expiración.

## 4. Proteger rutas por rol

Crear archivo: `security/JwtFiltro.java`. Lee el token de cada petición y le dice a Spring quién es y qué rol tiene.

```java
@Component
public class JwtFiltro extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtFiltro(JwtService jwtService) { this.jwtService = jwtService; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            try {
                Claims datos = jwtService.leer(encabezado.substring(7));
                String rol = "ROLE_" + datos.get("rol", String.class);
                UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                    datos.getSubject(), null, List.of(new SimpleGrantedAuthority(rol)));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
```

Completar `config/SeguridadConfig.java`:

```java
@Bean
public SecurityFilterChain filtros(HttpSecurity http, JwtFiltro jwtFiltro) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(reglas -> reglas
            .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
            .anyRequest().authenticated())
        .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}

@Bean
public CorsConfigurationSource cors(@Value("${cors.origen:http://localhost:5173}") String origen) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(origen));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
    fuente.registerCorsConfiguration("/**", config);
    return fuente;
}
```

Explicación:
- `csrf.disable()`: no hay cookies de sesión, el token va en el encabezado.
- `STATELESS`: el servidor no recuerda sesiones; todo viene en el token.
- `permitAll`: login, registro y Swagger se abren sin token.
- `hasRole("ADMINISTRADOR")`: Spring busca la autoridad `ROLE_ADMINISTRADOR`, por eso el filtro le agrega `ROLE_`.
- `cors`: permite que el frontend en el puerto 5173 llame a este puerto.

Resultado esperado: GET `/api/usuarios` sin token responde 401 o 403. Con el token de un administrador responde 200; con el de un cliente, 403. En Swagger, el candado "Authorize" permite pegar el token.

## 5. Desactivar en vez de borrar

La Forma E pide crear, editar y desactivar usuarios. Borrarlos rompería el historial de pedidos que los nombra.

Agregar a `UsuarioController`:

```java
@PatchMapping("/{id}/estado")
public Usuario cambiarEstado(@PathVariable Long id) { return service.cambiarEstado(id); }
```

Y a `UsuarioService`:

```java
public Usuario cambiarEstado(Long id) {
    Usuario actual = buscar(id);
    actual.setActivo(!actual.getActivo());
    return repository.save(actual);
}
```

`DELETE` puede quedar solo para pruebas, o quitarse.

## 6. Colección de Postman (evidencia de la Guía 15)

Crear la colección "ms-usuarios" con: registro, login (guardar el token en una variable), listar, buscar, crear, actualizar, cambiar estado y un caso de error 400 y uno de 404. Exportarla y guardarla en `docs/postman/ms-usuarios.json`.

## 7. Conectar el frontend

En el frontend (repo `ferreteria-los-maestros`):
- `.env` con `VITE_API_USUARIOS=http://localhost:8090`.
- Crear `src/services/usuarios.js` con funciones `login(datos)` y `registrar(datos)` que hagan `fetch` al backend.
- `Login.jsx` llama a `login`, guarda el `token` y el usuario. `utils/persistencia.js` deja de leer `usuariosAdmin`.
- `AdminUsuarios.jsx` usa `GET/POST/PUT/PATCH` del backend enviando `Authorization: Bearer <token>`.
- Los roles llegan en mayúsculas (`ADMINISTRADOR`): ajustar `utils/sesion.js`.

## 8. Repetir en los otros microservicios

Cada microservicio es una carpeta nueva al lado de `ms-usuarios`, creada con Spring Initializr con las mismas dependencias, y sigue los mismos pasos de la Guía 15: entidad, repositorio, servicio, controlador, `GlobalExceptionHandler`, Swagger y MySQL.

| Orden | Carpeta | Puerto | Base | Primera entidad |
|---|---|---|---|---|
| 2 | `ms-catalogo` | 8091 | `db_catalogo` | Producto |
| 3 | `ms-pedidos` | 8092 | `db_pedidos` | Pedido y DetallePedido |
| 4 | `ms-cuentas` | 8093 | `db_cuentas` | CuentaCorriente |
| 5 | `ms-contenido` | 8094 | `db_contenido` | Blog y MensajeContacto |

En cada uno:
- Copiar `JwtService` (solo `leer`), `JwtFiltro` y `SeguridadConfig` con sus propias reglas por rol, usando la misma `JWT_SECRET`.
- Las tablas, relaciones y endpoints de cada servicio están en `Ferreteria_modelo_backend.md` (secciones 3, 6 y 11).

Cierre con Git, después de cada paso:

```bash
git status
git add .
git commit -m "feat: descripción del paso"
git push
```
