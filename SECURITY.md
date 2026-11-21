# Política de seguridad

## Revisión de dependencias (2026-11-21)

Se auditaron las dependencias con `npm audit` (frontend) y con la base de datos
[OSV](https://osv.dev) sobre el árbol Maven realmente resuelto (backend), no solo
sobre las versiones declaradas en `pom.xml`.

### Frontend

- `npm audit`: **0 vulnerabilidades conocidas** (174 dependencias, 9 de producción).
- Dependencias de ejecución: React 19.2.7, React DOM 19.2.7, React Router DOM 7.18.2,
  React Icons 5.7.0.

### Backend

Parches aplicados en `backend/pom.xml` sobre Spring Boot 3.5.16:

| Componente | Antes | Después | CVEs corregidas |
| --- | --- | --- | --- |
| Apache Tomcat embebido | 10.1.55 | 10.1.62 | CVE-2026-55956, CVE-2026-65905, CVE-2026-65637, CVE-2026-65182, CVE-2026-68525, CVE-2026-53404, CVE-2026-55276 |
| Jackson (databind/core) | 2.21.4 | 2.21.8 | CVE-2026-59889, CVE-2026-54515, CVE-2026-91777, CVE-2026-83557, CVE-2026-68497, CVE-2026-77310, CVE-2026-19032, CVE-2026-91776, CVE-2026-89425, CVE-2026-89407, GHSA-mhm7-754m-9p8w |
| Log4j API | 2.24.3 | 2.25.5 | CVE-2026-49844 |

Verificación: `mvn dependency:tree` confirma las versiones parcheadas y
`mvn clean test` pasa con 206 tests (1 omitido), incluidos 7 nuevos tests de JWT.

### Actualización de JJWT (sin CVE)

JJWT se actualizó de 0.11.5 a 0.13.0 en `jjwt-api`, `jjwt-impl` y `jjwt-jackson`.
No corrige una CVE, pero adopta la API 0.12+ y sus endurecimientos (rechazo de
JSON con miembros duplicados, límite de iteraciones PBES2, correcciones JPMS).
`JwtUtils` se migró a `Jwts.parser().verifyWith(...).build().parseSignedClaims(...)`
y `Jwts.SIG.HS256`, manteniendo la compatibilidad con los tokens ya emitidos.

### Riesgo aceptado: Spring Framework 6.2.19

`spring-webmvc` 6.2.19 está afectado por:

- **CVE-2026-47890** — corrupción de stream en Server-Sent Events con fragments.
- **CVE-2026-47884** — SSRF/RCE en `XsltView` con mapeo `/**` y vista sin nombre explícito.

No existe parche en la rama 6.2.x; la corrección está en Spring Framework 7.0.9+
(Spring Boot 4.x).

**Justificación:** la aplicación no usa `SseEmitter`/SSE ni `XsltView`, y no hay un
mapeo `/**` que renderice vistas por nombre. Las condiciones de explotación no se
cumplen, por lo que el riesgo residual se considera bajo.

**Seguimiento:** revisar en cada actualización de Spring Boot 3.5.x y planificar la
migración a Spring Boot 4.x.

### Deuda técnica pendiente (sin CVE conocida)

- spring-dotenv 4.0.0 → 5.1.0.
- image-kit-java 3.0.0 → 3.9.0.
- Caffeine 3.1.8 → 3.3.1.

## Cómo reproducir la auditoría

```bash
# Frontend
cd frontend
npm audit

# Backend: versiones resueltas
cd backend
mvn dependency:tree

# Backend: CVEs conocidas (OSV-Scanner)
osv-scanner --recursive .
```

El workflow `.github/workflows/security.yml` ejecuta estas comprobaciones en cada
push/PR y semanalmente.

## Reportar una vulnerabilidad

No abras un issue público con detalles explotables. Contacta de forma privada con
el mantenedor del repositorio e incluye versión afectada, impacto y pasos de
reproducción.
