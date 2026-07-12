# Arquitectura

## Tecnologías

### Backend

- Java 21
- Spring Boot 3
- Spring Data MongoDB
- Maven

### Frontend

- React
- Vite
- Axios
- React Router

### Base de datos

- MongoDB Atlas

---

# Arquitectura General

```text
React
  │
  ▼
Spring Boot REST API
  │
  ▼
MongoDB
```

---

# Estructura Backend

```text
controller
service
repository
model
dto
config
exception
```

---

# Estructura Frontend

```text
pages
components
services
hooks
layouts
router
```

---

# Entidad Principal

Figura

- id
- nombre
- descripcion
- categoria
- dificultad
- autor
- imagenPrincipal
- materiales