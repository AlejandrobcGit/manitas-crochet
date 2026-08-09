# Backend de Manitas Crochet

Este módulo contiene la API REST de Manitas Crochet, desarrollada con Spring Boot y conectada a MongoDB. Además del catálogo del MVP, la versión 2 incorpora autenticación JWT, roles, gestión de usuarios, favoritos, valoraciones, comentarios, verificación de correo y recuperación de contraseña.

## 🧩 Funcionalidades principales

- Gestión CRUD de figuras, categorías y colores.
- Búsqueda de figuras por nombre y categoría mediante DTOs de listado y detalle.
- Dificultad, autor, colores, dimensiones y varias imágenes por figura.
- Autenticación stateless con JWT, refresh token y roles `USER`/`ADMIN`.
- Registro, login, logout, verificación de email y recuperación de contraseña.
- Favoritos por usuario, valoraciones de 1 a 5 y comentarios por figura.
- Validación de datos y manejo centralizado de excepciones.
- CORS preparado para el frontend local.

## 🛠️ Tecnologías

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Security
- Spring Data MongoDB
- Spring Validation
- JJWT
- Spring Mail
- Spring Dotenv
- Lombok
- Maven

## 📦 Requisitos previos

- Java 21 instalado
- Maven disponible en la línea de comandos
- Acceso a una base de datos MongoDB.
- Cuenta SMTP de Gmail o un servidor SMTP compatible.

## ⚙️ Variables de entorno

El backend usa estas variables de entorno:

```bash
MONGODB_URI=mongodb://localhost:27017/manitas-crochet
SERVER_PORT=8080
SERVER_ADDRESS=localhost
APP_PROTOCOLO=http
APP_FRONTEND_URL=http://localhost:5173
jwt.secret=una-clave-secreta-larga
EMAIL_ADDRESS=tu-cuenta@gmail.com
EMAIL_PASSWORD=tu-contraseña-de-aplicación
```

Estas variables pueden definirse en un archivo `.env` del backend o en el entorno del proceso. No publiques credenciales reales.

## ▶️ Ejecutar el proyecto

Desde la raíz del proyecto:

```bash
cd backend
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
cd backend
mvnw.cmd spring-boot:run
```

## 🔗 Endpoints principales

### Figuras

- `GET /api/figuras?nombre=&categoriaId=` — listado, búsqueda y filtro.
- `GET /api/figuras/{id}` — detalle y resumen de valoraciones.
- `POST /api/figuras` — crear con multipart (`data`, `imagenPrincipal`, `imagenesSecundarias`). Solo ADMIN.
- `PUT /api/figuras/{id}` — actualizar datos e imágenes. Solo ADMIN.
- `DELETE /api/figuras/{id}` — eliminar. Solo ADMIN.

### Categorías

- `GET /api/categorias` y `GET /api/categorias/{id}`.
- `POST /api/categorias`, `PUT /api/categorias/{id}` y `DELETE /api/categorias/{id}` — solo ADMIN.

### Colores

- `GET /api/color` y `GET /api/color/{id}`.
- `POST /api/color`, `PUT /api/color/{id}` y `DELETE /api/color/{id}` — solo ADMIN.

### Dificultades

- `GET /api/dificultades` — devuelve los valores del enum de dificultad.

### Imágenes

- `POST /api/imagenes/{id}` — subir o reemplazar la imagen principal.
- `GET /api/imagenes/{filename}` — recuperar una imagen.
- `GET /api/imagenes/url/{id}` — obtener la URL de la imagen principal.
- `DELETE /api/imagenes/{id}` — eliminar la imagen principal.

### Autenticación

- `POST /auth/signup` — registro de usuario.
- `POST /auth/signin` — login y emisión de tokens.
- `POST /auth/refresh` — renovar el access token mediante cookie.
- `POST /auth/logout` — invalidar la cookie de refresh.
- `GET /auth/enviarcorreoverificar` y `GET /auth/verificar?token=...` — verificación de correo.
- `POST /auth/enviarCorreoRecuperar-contrasena?email=...` — solicitar recuperación.
- `POST /auth/restablecer-contrasena` — cambiar la contraseña con token.
- `POST /auth/admin/crear-admin` — crear administrador; requiere rol ADMIN.

### Interacción de usuarios

- `GET /api/favorito` y `POST /api/favorito/{figuraId}` — consultar o alternar favoritos; requiere autenticación.
- `POST /api/valoraciones/{figuraId}` — guardar una valoración; requiere autenticación.
- `GET /api/comentarios/figura/{figuraId}` — listar comentarios públicamente.
- `GET /api/comentarios/figura/{figuraId}/usuario`, `POST /api/comentarios` y `DELETE /api/comentarios/{comentarioId}` — gestionar comentarios; requiere autenticación.

## 📁 Estructura relevante

```text
src/main/java/
├── controller/     # Controladores REST
├── service/        # Lógica de negocio
├── repository/     # Repositorios MongoDB
├── model/          # Entidades del dominio
├── dto/            # Objetos de transferencia
├── exception/      # Manejo de errores
├── security/       # JWT, filtros y configuración de seguridad
└── config/         # Carga inicial, CORS y configuración
```

## 📌 Nota

El backend escucha normalmente en `http://localhost:8080` y permite orígenes locales `http://localhost:*`. El frontend se ejecuta normalmente en `http://localhost:5173`.
