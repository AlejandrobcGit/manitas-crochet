# 🧶 Manitas Crochet

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-47A248?logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-24.0-2496ED?logo=docker&logoColor=white)
![Estado](https://img.shields.io/badge/Estado-Versi%C3%B3n%203-brightgreen)
![Licencia](https://img.shields.io/badge/Licencia-MIT-blue)

Manitas Crochet es una aplicación full stack para publicar y gestionar un catálogo digital de figuras de crochet y amigurumis. La versión 3 incorpora un dashboard de estadísticas, almacenamiento de imágenes en la nube mediante ImageKit, contenedorización con Docker y monitorización con Actuator/Prometheus.

## ✨ Funcionalidades

### Catálogo público

- Catálogo público de figuras con tarjetas y detalle.
- Búsqueda por nombre y filtrado por categoría.
- Filtro local de favoritos.
- Página de detalle con descripción, dificultad, autor, colores, dimensiones, galería de imágenes y valoraciones.
- Comentarios públicos asociados a cada figura.

### Usuarios y seguridad

- Registro e inicio de sesión.
- Autenticación mediante JWT y refresh token almacenado en cookie HTTP-only.
- Roles `USER` y `ADMIN`, con protección del panel de administración.
- Verificación de correo electrónico.
- Recuperación y restablecimiento de contraseña mediante correo.
- Cierre de sesión y renovación automática de la sesión.

### Administración

- CRUD de figuras, categorías y colores.
- Gestión de dificultad, autor, materiales y dimensiones.
- Carga de una imagen principal y varias imágenes secundarias.
- Gestión de favoritos y valoraciones desde el catálogo.

### Dashboard de estadísticas (v3)

- 5 tarjetas KPI: total figuras, visualizaciones, favoritos, comentarios y valoración media.
- 4 rankings Top-10: por visualizaciones, favoritos, comentarios y valoración.
- Sección de tendencias: Top-10 figuras con más visualizaciones en los últimos 30 días.
- 4 gráficas de evolución mensual: visualizaciones, favoritos, comentarios y valoraciones.
- Datos optimizados con caché de 30 s (Caffeine) y agregaciones en paralelo.

### Infraestructura (v3)

- Almacenamiento de imágenes en la nube mediante ImageKit con compresión WebP.
- Contenedorización completa con Docker y Docker Compose.
- Monitorización con Spring Actuator y métricas Prometheus.

## 🏗️ Arquitectura

El proyecto sigue una arquitectura cliente-servidor simple y clara:

```text
React / Vite
   │
   └─> Spring Boot API REST
          │
          ├─> MongoDB
          └─> ImageKit (imágenes)
```

## 🛠️ Tecnologías principales

- Backend: Java 21, Spring Boot 3.5.16, Spring Web, Spring Security, Spring Data MongoDB, Validation, JWT, Java Mail, ImageKit SDK, Caffeine, Actuator/Prometheus y Maven.
- Frontend: React 19, Vite 8, React Router 7, React Icons, Fetch API y CSS personalizado.
- Base de datos: MongoDB.
- Almacenamiento de imágenes: ImageKit (nube).
- Infraestructura: Docker, Docker Compose, Nginx.

## 📁 Estructura del repositorio

```text
manitas-crochet/
├── backend/           # API REST y lógica de negocio
├── frontend/          # Aplicación React/Vite
├── docs/              # Documentación de arquitectura y requisitos
├── docker-compose.yml # Orquestación de contenedores
└── README.md          # Documentación general del proyecto
```

## ▶️ Inicio rápido

### Opción 1: Docker Compose (recomendado)

Requisitos:
- Docker y Docker Compose instalados.

```bash
docker compose up --build
```

La aplicación quedará disponible en:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
```

### Opción 2: Ejecución manual

#### 1. Configurar el backend

Requisitos:
- Java 21
- Maven
- MongoDB accesible mediante la variable de entorno `MONGODB_URI`.
- Cuenta SMTP, necesaria para verificación de correo y recuperación de contraseña.
- Cuenta ImageKit para almacenamiento de imágenes.

Variables de entorno necesarias:

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

Ejecutar:

```bash
cd backend
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
cd backend
mvnw.cmd spring-boot:run
```

La API estará disponible en:

```text
http://localhost:8080
```

#### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

La aplicación quedará disponible en:

```text
http://localhost:5173
```

## 🔗 Rutas principales

- Frontend público: `/` y `/figuras/:id`.
- Autenticación: `/login`, `/signup`, `/verificar-email` y `/recuperar-contrasena`.
- Panel de administración: `/administracion` (incluye dashboard de estadísticas).
- API: `/auth/**`, `/api/figuras`, `/api/categorias`, `/api/color`, `/api/imagenes`, `/api/favorito`, `/api/valoraciones`, `/api/comentarios` y `/api/dashboard/kpis`.

## 📌 Evolución de versiones

- **Versión 1:** catálogo, búsqueda, filtros, CRUD de figuras, categorías, colores e imágenes.
- **Versión 2:** autenticación y autorización, verificación de correo, recuperación de contraseña, favoritos, valoraciones, comentarios y galería con varias imágenes.
- **Versión 3:** dashboard de estadísticas, almacenamiento de imágenes en ImageKit, Dockerización, monitorización con Actuator/Prometheus.
- **Por venir:** descarga de patrones PDF y tienda online.

## 👤 Autor

Proyecto desarrollado como ejercicio de portfolio y aprendizaje full stack.

## 📄 Licencia

Este proyecto se distribuye bajo la licencia MIT.
