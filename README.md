# Ferretería Los Maestros - Backend

Backend de la tienda Ferretería Los Maestros (DSY1104 Desarrollo Fullstack II, Duoc UC). Está dividido en microservicios Spring Boot independientes: cada uno tiene su carpeta, su `pom.xml`, su `application.properties`, su puerto y su base de datos.

El frontend está en https://github.com/GiuliannoInostroza/front-ferreteria-los-maestros

| Microservicio | Carpeta | Puerto | Base de datos | Responsable | Estado |
|---|---|---|---|---|---|
| Usuarios y autenticación | `ms-usuarios` | 8090 | `db_usuarios` | Giulianno Inostroza | Listo |
| Catálogo | `ms-catalogo` | 8091 | `db_catalogo` | Nicolás Pérez | Pendiente |
| Pedidos | `ms-pedidos` | 8092 | `db_pedidos` | Giulianno Inostroza | Listo |
| Cuentas corrientes | `ms-cuentas` | 8093 | `db_cuentas` | Nicolás Pérez | Pendiente |
| Contenido | `ms-contenido` | 8094 | `db_contenido` | Agustín Pérez | Pendiente |

## Cómo se reparte la seguridad (Guía 17)

- `ms-usuarios` es el servicio de autenticación: guarda los usuarios con su contraseña en BCrypt, recibe el login y **emite** el JWT (emisor `ms-usuarios`, correo como `subject` y el claim `rol`). También protege su propia administración de usuarios.
- Los demás microservicios son servicios de recursos: **validan** el JWT con la misma `JWT_SECRET` y exigen el emisor `ms-usuarios`, sin consultar la base de usuarios.
- El token dura 1 hora (`app.jwt.expiration-ms=3600000`) y no se renueva: al vencer, las API responden 401 y React vuelve a pedir el inicio de sesión.
- Roles: `Administrador`, `Vendedor` y `Cliente`.

## Variables de entorno

Cada microservicio lee sus claves desde el entorno; ninguna se escribe en el código ni en Git. En Linux se guardan en un `.env` dentro de la carpeta del microservicio (ignorado por Git) y se cargan con `set -a && . ./.env && set +a`.

| Variable | ms-usuarios | ms-pedidos | Uso |
|---|---|---|---|
| `DB_URL` | opcional | opcional | Dirección de la base, si no es la local |
| `DB_USER` | sí | sí | Cuenta de MySQL del servicio |
| `DB_PASSWORD` | sí | sí | Contraseña de esa cuenta |
| `JWT_SECRET` | sí | sí | Clave Base64 de 32 bytes, **la misma** en todos los microservicios |
| `ADMIN_CORREO` | opcional | no | Correo del administrador inicial |
| `ADMIN_PASSWORD` | sí | no | Contraseña del administrador inicial |

La `JWT_SECRET` se genera una sola vez y se comparte por privado con el equipo:

```bash
openssl rand -base64 32
```

## Crear las bases de datos

En el cliente MySQL o MariaDB, con un usuario administrador (`sudo mariadb` en Linux):

```sql
CREATE DATABASE db_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'usuarios_app'@'localhost' IDENTIFIED BY 'CAMBIAR_CLAVE';
GRANT ALL PRIVILEGES ON db_usuarios.* TO 'usuarios_app'@'localhost';

CREATE DATABASE db_pedidos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pedidos_app'@'localhost' IDENTIFIED BY 'CAMBIAR_CLAVE';
GRANT ALL PRIVILEGES ON db_pedidos.* TO 'pedidos_app'@'localhost';

FLUSH PRIVILEGES;
```

## Levantar los microservicios

Cada uno en su propia terminal, dentro de su carpeta:

```bash
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

En Windows (PowerShell), definir las variables con `$env:NOMBRE="valor"` y ejecutar `.\mvnw.cmd spring-boot:run`.

Maven y sus dependencias se descargan desde el mirror de Google de Maven Central (`.mvn/settings.xml`, cargado por `.mvn/maven.config`, y la dirección del wrapper en `.mvn/wrapper/maven-wrapper.properties`). Así compila también en redes que bloquean el repositorio oficial, como la de Duoc, sin configurar nada en cada computador.

Al iniciar por primera vez, `ms-usuarios` crea el administrador definido en `ADMIN_CORREO` y `ADMIN_PASSWORD`.

| Swagger | Dirección |
|---|---|
| ms-usuarios | http://localhost:8090/swagger-ui/index.html |
| ms-pedidos | http://localhost:8092/swagger-ui/index.html |

Para probar una ruta protegida en Swagger: ejecutar `POST /api/auth/login` en ms-usuarios, copiar el `token`, presionar **Authorize** en el otro Swagger y pegarlo.

## Endpoints de ms-usuarios

| Operación | Método y ruta | Acceso | Respuesta |
|---|---|---|---|
| Iniciar sesión | POST /api/auth/login | Público | 200 con token, 401 si las credenciales no sirven o la cuenta está inactiva |
| Crear cuenta | POST /api/auth/registro | Público | 201 como Cliente, 409 si el correo ya existe |
| Listar usuarios | GET /api/usuarios | Administrador | 200 |
| Buscar usuario | GET /api/usuarios/{id} | Administrador | 200 o 404 |
| Crear usuario | POST /api/usuarios | Administrador | 201, 400 o 409 |
| Editar, asignar rol o desactivar | PUT /api/usuarios/{id} | Administrador | 200 o 404 |
| Eliminar usuario | DELETE /api/usuarios/{id} | Administrador | 204 o 404 |

Las respuestas de usuarios nunca incluyen la contraseña.

## Endpoints de ms-pedidos

| Operación | Método y ruta | Acceso | Respuesta |
|---|---|---|---|
| Crear pedido | POST /api/pedidos | Con sesión | 201 en estado Pendiente; el cliente sale del token |
| Mis pedidos | GET /api/pedidos/mios | Con sesión | 200 con los pedidos del usuario del token |
| Listar pedidos | GET /api/pedidos | Administrador y Vendedor | 200 |
| Buscar pedido | GET /api/pedidos/{id} | Administrador y Vendedor | 200 o 404 |
| Cambiar estado | PUT /api/pedidos/{id} | Administrador y Vendedor | 200 o 404 |

Estados: Pendiente, Confirmado, En preparación, Listo para retiro, En despacho, Entregado y Cancelado.

## Matriz de seguridad

| Prueba | Destino | Resultado esperado |
|---|---|---|
| Login válido | ms-usuarios 8090 | 200 y JWT |
| Login inválido | ms-usuarios 8090 | 401 |
| GET /api/usuarios sin token | ms-usuarios 8090 | 401 |
| GET /api/usuarios con token Cliente | ms-usuarios 8090 | 403 |
| GET /api/usuarios con token Administrador | ms-usuarios 8090 | 200 |
| POST /api/pedidos con token Cliente | ms-pedidos 8092 | 201 |
| GET /api/pedidos con token Cliente | ms-pedidos 8092 | 403 |
| GET /api/pedidos con token Vendedor | ms-pedidos 8092 | 200 |
| Token alterado | ms-pedidos 8092 | 401 |
