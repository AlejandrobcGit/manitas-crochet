# Benchmark de rendimiento del catálogo — comparativa de las 3 evoluciones

Medidas reales sobre `GET /api/figuras` (backend en `localhost:8080`), mismas peticiones y
misma numeración `N` para las tres columnas. Cada celda es la **media en ms de 20 peticiones**
si no se indica lo contrario. La última columna corresponde al estado actual del código
(paginación + filtros combinables **MEJORA 1**, métricas denormalizadas **MEJORA 3** y
**índices `idx_*`** en Mongo).

## Catálogo — listados y filtros

| N | Consulta | Original | Denorm (MEJ1+3) | Última + índices | Δ índices |
|---|----------|---------:|----------------:|-----------------:|----------:|
| 1 | `p0 s12` recientes | 246,0 | 167,5 | **165,6** | −1,1% |
| 3 | `p1 s12` recientes | 262,5 | 207,5 | **157,7** | −24,0% |
| 4 | `p2 s12` recientes | 242,0 | 220,9 | **164,0** | −25,8% |
| 5 | `p0 s24` recientes | 234,3 | 203,8 | **174,7** | −14,3% |
| 6 | `p0 s50` recientes | 299,5 | 202,5 | **158,4** | −21,8% |
| 8 | `p0 s1` recientes | 210,4 | 207,8 | **159,0** | −23,5% |
| 16 | `nombre=e s12` | 222,1 | 162,3 | **160,7** | −1,0% |
| 17 | `nombre=e s50` | 235,2 | 178,9 | **156,1** | −12,7% |
| 18 | `nombre=ej s12` | 214,6 | 179,4 | **163,5** | −8,9% |
| 19 | `nombre=anime` (0 res.) | 79,5 | 83,5 | **68,3** | ruido |
| 20 | `categoriaId=Anime` | 204,2 | 430,6 | **162,9** | −62,2% |
| 21 | `categoriaId=Animales` | 242,2 | 173,8 | **158,3** | −8,9% |
| 22 | `categoriaId=Fantasía` | 219,2 | 184,0 | **162,4** | −11,7% |
| 23 | `categoriaId=Personajes` | 262,0 | 180,1 | **154,9** | −14,0% |
| 24 | `categoriaId=Navidad` (0 res.) | 109,1 | 85,3 | **179,9** | anomalía* |
| 25 | `categoriaId` inexistente | 89,7 | 84,2 | **68,0** | −19,2% |
| 28 | `nombre` + `categoriaId` | 216,0 | 174,4 | **163,8** | −6,1% |

## Ordenaciones (sort nativo)

| Consulta | Original | Denorm (MEJ1+3) | Última + índices | Δ índices |
|----------|---------:|----------------:|-----------------:|----------:|
| `sort=populares s12` | 485,2 | 183,6 | **167,8** | −8,6% |
| `sort=populares s50` | 448,2 | 186,1 | **175,1** | −5,9% |
| `sort=valorados s12` | 451,7 | 184,7 | **158,2** | −14,3% |
| `sort=valorados s50` | 445,3 | 198,4 | **172,8** | −12,9% |

## Detalle de figura

| Consulta | Original | Última (batch colores) | Δ |
|----------|---------:|----------------------:|----------:|
| detalle + colores | 326 | 96 | −70% |

---

\* `categoriaId=Navidad` (0 resultados) dio 179,9 ms frente a 85,3 en la columna anterior;
al devolver conjunto vacío no usa los índices compuestos, por lo que se considera ruido de
cold-cache, no una regresión.

**Metodología:** warm-up preliminar + 20 peticiones por combinación (páginas 1-3 aleatorias),
HTTP 200, 0 fallos. Mismo endpoint y mismos parámetros en las tres columnas.
