# Frontend de Manitas Crochet

Este directorio contiene la interfaz web de Manitas Crochet, desarrollada con React y Vite. La versión 3 añade el dashboard de estadísticas de administración con KPIs, rankings, tendencias y gráficas de evolución, además de la Dockerización del despliegue.

## 🚀 Tecnologías

- React 19
- Vite 8
- React Router 7
- React Icons
- CSS personalizado (sin framework de UI)
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
- `/administracion` — panel de administración (incluye el dashboard de estadísticas).
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
- `/api/dashboard/kpis`
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

## ✨ Funcionalidades de la versión 3

- Dashboard de estadísticas accesible desde el panel de administración con:
  - 5 tarjetas KPI: total de figuras, visualizaciones, favoritos, comentarios y valoración media.
  - 4 rankings Top-10 (por visualizaciones, favoritos, comentarios y valoración) en pestañas.
  - Sección de tendencias: Top-10 figuras más vistas en los últimos 30 días con barras horizontales.
  - 4 gráficas de evolución mensual (visualizaciones, favoritos, comentarios y valoraciones) en pestañas.
- Visualización responsive adaptada a escritorio y móvil.
- Despliegue contenedorizado con Docker y Nginx.

## 📁 Estructura relevante

```text
src/
├── components/     # Componentes reutilizables
├── pages/          # Páginas principales del sitio (incluye admin/DashboardPage)
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

También se puede ejecutar en contenedor:

```bash
docker compose up --build frontend
```

El frontend quedará disponible en:

```text
http://localhost:3000
```

## 🔐 Flujo de autenticación

El frontend envía el access token en las peticiones protegidas y utiliza `credentials: include` para conservar el refresh token HTTP-only. Al cargar la aplicación intenta restaurar la sesión; si el usuario no está autenticado, las rutas protegidas redirigen a `/login`.

## 🧪 Comprobaciones

Antes de integrar cambios, se recomienda ejecutar:

```bash
npm run lint
npm run build
```
