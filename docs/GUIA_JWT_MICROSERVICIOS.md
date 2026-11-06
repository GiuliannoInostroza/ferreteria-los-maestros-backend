# Validar el JWT en otro microservicio

Esta es la parte "servicio de recursos" de la Guía 17 (secciones 11 a 13), ya aplicada en `ms-pedidos`. Sirve para `ms-catalogo`, `ms-cuentas` y `ms-contenido`. El servicio **no** emite tokens ni consulta usuarios: solo valida el token que emite `ms-usuarios`.

## 1. Dependencias

Agregar en el `pom.xml` del microservicio, dentro de `dependencies`: Spring Security (`spring-boot-starter-security`) y las tres de JJWT 0.12.6 (`jjwt-api`, `jjwt-impl` y `jjwt-jackson`, las dos últimas con `runtime`). Copiarlas desde `ms-pedidos/pom.xml`.

## 2. Propiedades

Agregar en `src/main/resources/application.properties`:

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
app.jwt.secret=${JWT_SECRET}
app.jwt.issuer=ms-usuarios
```

`JWT_SECRET` va en el `.env` del microservicio con el **mismo valor** que usa `ms-usuarios`. Se pide por privado, nunca por GitHub.

## 3. Archivos que se copian de ms-pedidos

Cambiando solo el `package` (`com.los_maestros.pedidos` por el del microservicio):

| Archivo | Para qué |
|---|---|
| `security/JwtService.java` | Valida firma, emisor y expiración del token |
| `security/JwtAuthenticationFilter.java` | Lee `Authorization: Bearer` y registra el correo y el rol |
| `config/SecurityConfig.java` | Reglas por rol, respuesta 401 sin token y CORS para el puerto 5173 |
| `config/OpenApiConfig.java` | Botón Authorize en Swagger (cambiar título y descripción) |

## 4. Reglas por rol

En `SecurityConfig`, dentro de `authorizeHttpRequests`, se dejan las dos primeras reglas (OPTIONS y Swagger) y se cambian las demás por las del microservicio. El orden importa: Spring usa la primera regla que coincide, y la última siempre es `.anyRequest().denyAll()`.

Ejemplo para `ms-catalogo` (cualquiera con sesión ve productos; solo Administrador y Vendedor los modifican):

```java
.requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
.requestMatchers("/api/productos/**").hasAnyRole("Administrador", "Vendedor")
.anyRequest().denyAll())
```

`hasRole("Administrador")` exige ese rol exacto; `hasAnyRole` acepta cualquiera de los indicados. El filtro agrega `ROLE_` delante del rol del token.

## 5. Saber quién hace la petición

Dentro de un controlador, el correo del usuario del token se obtiene así (ver `PedidoController`):

```java
SecurityContextHolder.getContext().getAuthentication().getName()
```

## 6. Comprobar

| Prueba | Resultado esperado |
|---|---|
| Sin token | 401 |
| Token con rol sin permiso | 403 |
| Token con rol permitido | 200 o 201 |
| Token alterado | 401 |

Para obtener un token: `POST http://localhost:8090/api/auth/login` con `{ "correo": "...", "password": "..." }`.
