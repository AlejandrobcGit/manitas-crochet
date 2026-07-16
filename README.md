# 🧶 Manitas Crochet

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-47A248?logo=mongodb&logoColor=white)
![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-yellow)
![Licencia](https://img.shields.io/badge/Licencia-MIT-blue)

> *Creaciones únicas tejidas con imaginación*

Manitas Crochet es una aplicación Full Stack desarrollada con **Java, Spring Boot, React y MongoDB** para gestionar y visualizar un catálogo digital de figuras de crochet y amigurumis artesanales.

El proyecto nace con el objetivo de ofrecer una plataforma moderna donde explorar, organizar y administrar creaciones tejidas a mano, proporcionando una experiencia visual intuitiva y una arquitectura escalable basada en tecnologías actuales.

---

## 📖 Descripción

La aplicación permite gestionar un catálogo de figuras de crochet mediante una interfaz moderna y responsive.

Los usuarios pueden:

- Explorar el catálogo de figuras.
- Buscar figuras por nombre.
- Filtrar por categoría y nivel de dificultad.
- Consultar información detallada de cada creación.
- Registrar nuevas figuras.
- Editar figuras existentes.
- Eliminar figuras del catálogo.

---

## ✨ Características principales

### Catálogo Visual

Visualización de todas las figuras en formato de tarjetas.

### Gestión de Figuras

Operaciones CRUD completas:

- Crear
- Consultar
- Actualizar
- Eliminar

### Sistema de Búsqueda

Búsqueda rápida por nombre de figura.

### Sistema de Filtros

Filtrado por:

- Categoría
- Nivel de dificultad

### Diseño Responsive

Adaptado para:

- 📱 Móvil
- 💻 Escritorio
- 📟 Tablet

---

## 🏗️ Arquitectura

El proyecto sigue una arquitectura cliente-servidor:

```text
┌─────────────┐
│   React     │
│ Frontend UI │
└──────┬──────┘
       │ HTTP/REST
       ▼
┌─────────────┐
│ Spring Boot │
│ REST API    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  MongoDB    │
│  Database   │
└─────────────┘
```

---

## 🛠️ Tecnologías

### Backend

- Java 21
- Spring Boot 3
- Spring Data MongoDB
- Maven
- Lombok
- Bean Validation

### Frontend

- React
- Vite
- React Router
- Axios
- Tailwind CSS

### Base de Datos

- MongoDB Atlas

### Control de versiones

- Git
- GitHub

---

## 📂 Estructura del Proyecto

```text
manitas-crochet/
│
├── backend/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   ├── dto/
│   └── config/
│
├── frontend/
│   ├── pages/
│   ├── components/
│   ├── services/
│   ├── hooks/
│   ├── router/
│   └── layouts/
│
├── docs/
│
└── README.md
```

---

## 📊 Modelo de Datos

Ejemplo de documento almacenado en MongoDB:

```json
[
  {
    "id": "6a556072919c8e369d5dba43",
    "nombre": "Pikachu",
    "descripcion": "Figura amigurumi inspirada en Pokémon",
    "categoria": "Anime",
    "dificultad": "PRINCIPIANTE",
    "autor": "Alejo",
    "imagenPrincipal": "pikachu.jpg",
    "colores": [
      {
        "nombre": "Amarillo",
        "codigo": "#FFFF00"
      },
      {
        "nombre": "Negro",
        "codigo": "#000000"
      }
    ]
  }
]
```
---

## 🔮 Roadmap

### Versión 1.0

- [x] Diseño funcional
- [ ] Catálogo de figuras
- [ ] CRUD completo
- [ ] Búsquedas
- [ ] Filtros
- [ ] Responsive Design

### Versión 2.0

- [ ] Autenticación de usuarios
- [ ] Favoritos
- [ ] Múltiples imágenes
- [ ] Valoraciones
- [ ] Comentarios
- [ ] Descarga de patrones PDF

### Versión 3.0

- [ ] Gestión de pedidos
- [ ] Tienda online
- [ ] Panel de administración
- [ ] Dashboard estadístico

---

## 📸 Capturas de Pantalla

Próximamente.

### Página principal

```text
[ Captura pendiente ]
```

### Detalle de figura

```text
[ Captura pendiente ]
```

---

## ⚙️ Instalación

### Clonar repositorio

```bash
git clone https://github.com/TU-USUARIO/manitas-crochet.git
```

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Servidor:

```text
http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Aplicación:

```text
http://localhost:5173
```

---

## 🎯 Objetivo del Proyecto

Este proyecto forma parte de mi portfolio como desarrollador Full Stack y tiene como objetivo demostrar conocimientos en:

- Arquitectura REST
- Desarrollo Backend con Spring Boot
- Desarrollo Frontend con React
- Bases de datos NoSQL
- Gestión de APIs
- Diseño responsive
- Control de versiones con Git

---

## 👨‍💻 Autor

**Alejandro Blanco**

Desarrollador Full Stack en formación especializado en Java, Spring Boot y React.

---

## 📄 Licencia

Este proyecto está distribuido bajo licencia MIT.