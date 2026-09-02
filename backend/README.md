# Backend de Manitas Crochet

Este módulo contiene la API REST de Manitas Crochet, desarrollada con Spring Boot y conectada a MongoDB. La versión 3 incorpora un dashboard de estadísticas con caché y agregaciones en paralelo, almacenamiento de imágenes en ImageKit y monitorización con Actuator/Prometheus.

## 🧩 Funcionalidades principales

- Gestión CRUD de figuras, categorías y colores.
- Búsqueda de figuras por nombre y categoría mediante DTOs de listado y detalle.
- Dificultad, autor, colores, dimensiones y varias imágenes por figura.
- Autenticación stateless con JWT, refresh token y roles `USER`/`ADMIN`.
- Registro, login, logout, verificación de email y recuperación de contraseña.
- Favoritos por usuario, valoraciones de 1 a 5 y comentarios por figura.
- Dashboard de estadísticas: KPIs, rankings Top-10, tendencias y evolución mensual.
- Almacenamiento de imágenes en ImageKit con compresión WebP.
- Caché de 30 s (Caffeine) para los KPIs del dashboard.
- Agregaciones MongoDB en paralelo mediante `CompletableFuture`.
- Monitorización con Spring Actuator y métricas Prometheus.
- Validación de datos y manejo centralizado de excepciones.
- CORS preparado para el frontend local.

## 🛠️ Tecnologías

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Security
- Spring Data MongoDB
- Spring Validation
- JJWT 0.11.5
- Spring Mail
- Spring Dotenv
- Spring Actuator + Micrometer (Prometheus)
- ImageKit SDK 3.0.0
- Caffeine 3.1.8
- WebP ImageIO 0.1.6
- Lombok
- Maven
- JaCoCo 0.8.13 (cobertura de código)

## 📦 Requisitos previos

- Java 21 instalado
- Maven disponible en la línea de comandos
- Acceso a una base de datos MongoDB.
- Cuenta SMTP de Gmail o un servidor SMTP compatible.
- Cuenta ImageKit para almacenamiento de imágenes en la nube.

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
IMAGEKIT_PUBLIC_KEY=tu-public-key
IMAGEKIT_PRIVATE_KEY=tu-private-key
IMAGEKIT_URL_ENDPOINT=tu-url-endpoint
```

Estas variables pueden definirse en un archivo `.env` del backend o en el entorno del proceso. No publiques credenciales reales.

## ▶️ Ejecutar el proyecto

### Con Docker

```bash
docker compose up --build backend
```

### Sin Docker

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

Las imágenes se almacenan en ImageKit. Los endpoints de imágenes gestionan la subida, eliminación y compresión a WebP.

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

### Dashboard

- `GET /api/dashboard/kpis` — KPIs, rankings Top-10, tendencias y evolución mensual. Solo ADMIN. Caché de 30 s.

### Monitorización

- `GET /actuator/health` — estado de la aplicación (público).
- `GET /actuator/info` — información de la aplicación (público).
- `GET /actuator/prometheus` — métricas Prometheus (solo ADMIN).

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
└── config/         # Carga inicial, CORS, caché, ImageKit y configuración
```

## 🐳 Docker

El backend se contenedoriza con una imagen multi-stage basada en `eclipse-temurin:21-jre`. La JVM se configura con soporte de contenedores y límite de memoria al 75 %.

```bash
docker compose up --build backend
```

## 📌 Nota

El backend escucha normalmente en `http://localhost:8080` y permite orígenes locales `http://localhost:*`. El frontend se ejecuta normalmente en `http://localhost:5173`.
