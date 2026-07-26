# Backend de Manitas Crochet

Este módulo contiene la API REST del proyecto, desarrollada con Spring Boot y conectada a MongoDB. Su responsabilidad es gestionar el catálogo de figuras, categorías, colores, dificultades e imágenes.

## 🧩 Funcionalidades principales

- Gestión de figuras con operaciones CRUD.
- Gestión de categorías, colores y dificultades.
- Carga y recuperación de imágenes asociadas a cada figura.
- Exposición de endpoints REST para consumo por el frontend.
- Validación de datos de entrada y manejo centralizado de excepciones.

## 🛠️ Tecnologías

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Data MongoDB
- Spring Validation
- Lombok
- Maven

## 📦 Requisitos previos

- Java 21 instalado
- Maven disponible en la línea de comandos
- Acceso a una base de datos MongoDB

## ⚙️ Variables de entorno

El backend usa estas variables de entorno:

```bash
MONGODB_URI=mongodb://localhost:27017/manitas-crochet
SERVER_PORT=8080
```

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

- GET /api/figuras
- GET /api/figuras/{id}
- POST /api/figuras
- PUT /api/figuras/{id}
- DELETE /api/figuras/{id}

### Categorías

- GET /api/categorias

### Colores

- GET /api/color

### Imágenes

- POST /api/imagenes/{id}
- GET /api/imagenes/{filename}
- GET /api/imagenes/url/{id}
- DELETE /api/imagenes/{id}

## 📁 Estructura relevante

```text
src/main/java/
├── controller/     # Controladores REST
├── service/        # Lógica de negocio
├── repository/     # Repositorios MongoDB
├── model/          # Entidades del dominio
├── dto/            # Objetos de transferencia
└── exception/      # Manejo de errores
```

## 📌 Nota

El backend espera que el frontend consuma sus endpoints en el puerto 8080, mientras que el frontend se ejecuta normalmente en 5173.
