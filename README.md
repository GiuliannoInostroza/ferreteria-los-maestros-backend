# Ferretería Los Maestros - Backend

Backend de la tienda Ferretería Los Maestros (DSY1104 Desarrollo Fullstack II, Duoc UC). Está dividido en microservicios Spring Boot; cada uno tiene su carpeta, su puerto y su base de datos MySQL.

| Microservicio | Carpeta | Puerto | Base de datos | Estado |
|---|---|---|---|---|
| Usuarios | `ms-usuarios` | 8081 | `db_usuarios` | En desarrollo |
| Catálogo | `ms-catalogo` | 8082 | `db_catalogo` | Pendiente |
| Pedidos | `ms-pedidos` | 8083 | `db_pedidos` | Pendiente |
| Cuentas corrientes | `ms-cuentas` | 8084 | `db_cuentas` | Pendiente |
| Contenido | `ms-contenido` | 8085 | `db_contenido` | Pendiente |

El frontend está en https://github.com/GiuliannoInostroza/front-ferreteria-los-maestros

## Cómo levantar ms-usuarios

1. Crear la base `db_usuarios` y la cuenta `usuarios_app` (comandos en `docs/GUIA_MS_USUARIOS.md`, paso 0).
2. Dentro de `ms-usuarios`, definir `DB_USER` y `DB_PASSWORD` como variables de entorno y ejecutar `./mvnw spring-boot:run` (en Windows `.\mvnw.cmd spring-boot:run`).
3. Abrir Swagger en http://localhost:8081/swagger-ui/index.html.

| Operación | Método y ruta | Respuesta |
|---|---|---|
| Listar | GET /api/usuarios | 200 y arreglo |
| Buscar | GET /api/usuarios/{id} | 200 o 404 |
| Crear | POST /api/usuarios | 201 y objeto, 400 si faltan datos |
| Actualizar | PUT /api/usuarios/{id} | 200 o 404 |
| Eliminar | DELETE /api/usuarios/{id} | 204 o 404 |

Lo que falta en ms-usuarios (rol como tabla, contraseña con BCrypt, login con JWT y permisos por rol) está explicado paso a paso en `docs/GUIA_MS_USUARIOS.md`.
