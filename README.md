# 🧶 Manitas Crochet

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-47A248?logo=mongodb&logoColor=white)
![Estado](https://img.shields.io/badge/Estado-En%20desarrollo-yellow)
![Licencia](https://img.shields.io/badge/Licencia-MIT-blue)

Manitas Crochet es una aplicación full stack para gestionar un catálogo digital de figuras de crochet y amigurumis. El proyecto combina un backend en Spring Boot con un frontend en React y Vite, permitiendo explorar, buscar y administrar piezas con imágenes y metadatos.

## ✨ Qué incluye actualmente

- Catálogo público de figuras con tarjetas y detalle.
- Búsqueda por nombre y filtrado por categoría o dificultad.
- Panel de administración para crear, editar y eliminar figuras, colores y categorías.
- Subida y visualización de imágenes asociadas a cada figura.
- API REST para consumo desde el frontend.

## 🏗️ Arquitectura

El proyecto sigue una arquitectura cliente-servidor simple y clara:

```text
React / Vite
   │
   └─> Spring Boot API REST
          │
          └─> MongoDB
```

## 🛠️ Tecnologías principales

- Backend: Java 21, Spring Boot 3.5.16, Spring Data MongoDB, Maven, Lombok, Validation.
- Frontend: React 19, Vite 8, React Router 7, CSS personalizado.
- Base de datos: MongoDB.
- Almacenamiento de archivos: sistema local de imágenes en el backend.

## 📁 Estructura del repositorio

```text
manitas-crochet/
├── backend/           # API REST y lógica de negocio
├── frontend/          # Aplicación React/Vite
├── docs/              # Documentación de arquitectura y requisitos
└── README.md          # Documentación general del proyecto
```

## ▶️ Inicio rápido

### 1. Backend

Requisitos:
- Java 21
- Maven
- MongoDB accesible mediante la variable de entorno MONGODB_URI

Variables de entorno necesarias:

```bash
MONGODB_URI=mongodb://localhost:27017/manitas-crochet
SERVER_PORT=8080
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

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

La aplicación quedará disponible en:

```text
http://localhost:5173
```

## 🔗 Puntos principales del sistema

- Frontend público: inicio y detalle de figuras.
- Panel admin: rutas bajo /administracion.
- API base del backend: /api/figuras, /api/categorias, /api/color, /api/imagenes.

## 📌 Estado actual

El proyecto ya cuenta con:
- CRUD de figuras.
- Gestión de categorías, colores y dificultades.
- Gestión de imágenes.
- Interfaz de administración funcional.

## �️ Roadmap

### Versión 1.0
- [x] Catálogo público de figuras
- [x] Búsqueda y filtros básicos
- [x] Gestión de figuras desde el panel de administración
- [x] Gestión de categorías y colores
- [x] Subida y visualización de imágenes

### Próximas mejoras
- [ ] Autenticación de usuarios
- [ ] Favoritos y valoraciones
- [ ] Mejoras en la experiencia de administración
- [ ] Optimización de imágenes y rendimiento
- [ ] Despliegue en producción

## �👤 Autor

Proyecto desarrollado como ejercicio de portfolio y aprendizaje full stack.

## 📄 Licencia

Este proyecto se distribuye bajo la licencia MIT.