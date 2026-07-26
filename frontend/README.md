# Frontend de Manitas Crochet

Este directorio contiene la interfaz web del proyecto, desarrollada con React y Vite. Su función es mostrar el catálogo de figuras y ofrecer un panel de administración para gestionar contenido.

## 🚀 Tecnologías

- React 19
- Vite 8
- React Router 7
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

## 🔌 Integración con el backend

El frontend consume la API del backend en los endpoints:

- `/api/figuras`
- `/api/categorias`
- `/api/color`
- `/api/imagenes`

Por defecto, la aplicación espera que el backend esté disponible en:

```text
http://localhost:8080
```

## 📁 Estructura relevante

```text
src/
├── components/     # Componentes reutilizables
├── pages/          # Páginas principales del sitio
├── contexts/       # Contextos de datos
├── hooks/          # Hooks personalizados
├── api/            # Utilidades para peticiones HTTP
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
