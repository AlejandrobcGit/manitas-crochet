# Frontend de Manitas Crochet

Este directorio contiene la interfaz web de Manitas Crochet, desarrollada con React y Vite. La versión 2 mantiene el catálogo del MVP y añade autenticación, favoritos, valoraciones, comentarios, verificación de correo, recuperación de contraseña y una galería de varias imágenes.

## 🚀 Tecnologías

- React 19
- Vite 8
- React Router 7
- React Icons
- CSS personalizado
- Fetch nativo para consumir la API REST del backend

## 📦 Scripts disponibles

Instalar dependencias:

```bash
npm install
```

Iniciar el entorno de desarrollo:

```bash
npm run dev
```

Compilar para producción:

```bash
npm run build
```

Revisar calidad de código con ESLint:

```bash
npm run lint
```

## 🧭 Rutas principales

- `/` — página de inicio con el catálogo de figuras.
- `/figuras/:id` — vista detallada de una figura.
- `/administracion` — panel de administración.
- `/login` — inicio de sesión y solicitud de recuperación de contraseña.
- `/signup` — registro de usuarios.
- `/verificar-email?token=...` — verificación de correo electrónico.
- `/recuperar-contrasena?token=...` — restablecimiento de contraseña.
- `/no-autorizado` — respuesta para usuarios sin permisos.

El panel `/administracion` está protegido y solo es accesible para usuarios con rol `ROLE_ADMIN`.

## 🔌 Integración con el backend

El frontend consume la API del backend en los endpoints:

- `/api/figuras`
- `/api/categorias`
- `/api/color`
- `/api/imagenes`
- `/api/favorito`
- `/api/valoraciones`
- `/api/comentarios`
- `/auth`

Por defecto, la aplicación espera que el backend esté disponible en:

```text
http://localhost:8080
```

La URL está configurada actualmente en los servicios del frontend como `http://localhost:8080`. Si se despliega en otro entorno, debe sustituirse por la URL del backend o centralizarse mediante `VITE_API_URL`.

## ✨ Funcionalidades de la versión 2

- Inicio de sesión, registro y restauración automática de la sesión.
- Protección de rutas y visualización de opciones según el rol del usuario.
- Favoritos persistidos por usuario y filtro de favoritos en el catálogo.
- Valoración de figuras con estrellas y media global.
- Lectura, creación y eliminación de comentarios.
- Galería con imagen principal y miniaturas de imágenes secundarias.
- Formularios de administración para figuras, categorías y colores.
- Mensajes de carga, validación y error en las peticiones.

## 📁 Estructura relevante

```text
src/
├── components/     # Componentes reutilizables
├── pages/          # Páginas principales del sitio
├── contexts/       # Contextos de datos
├── hooks/          # Hooks personalizados
├── api/            # Utilidades para peticiones HTTP
├── services/       # Servicios de acceso a datos
└── App.jsx         # Configuración de rutas
```

## ▶️ Ejecución rápida

```bash
cd frontend
npm install
npm run dev
```

Luego abre la URL mostrada por Vite en el navegador, normalmente:

```text
http://localhost:5173
```

## 🔐 Flujo de autenticación

El frontend envía el access token en las peticiones protegidas y utiliza `credentials: include` para conservar el refresh token HTTP-only. Al cargar la aplicación intenta restaurar la sesión; si el usuario no está autenticado, las rutas protegidas redirigen a `/login`.

## 🧪 Comprobaciones

Antes de integrar cambios, se recomienda ejecutar:

```bash
npm run lint
npm run build
```
