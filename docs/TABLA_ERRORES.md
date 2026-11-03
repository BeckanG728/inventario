# Tabla de errores — Sistema de Gestión de Inventario (Taller Mecánico)

Documento generado a partir del código actual: `GlobalExceptionHandler`, `TokenAuthenticationEntryPoint`,
`JwtAuthFilter`, `SecurityConfig` y los servicios de cada módulo.

## 1. Contrato de error

Todos los errores controlados (excepciones de dominio, validación y seguridad) usan el mismo formato:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "La petición contiene campos no válidos",
  "details": [
    {
      "field": "nombre",
      "reason": "El nombre es obligatorio"
    }
  ],
  "endpoint": "/api/productos",
  "timestamp": "2026-10-31T15:04:05Z"
}
```

- `details` solo aparece en errores de Bean Validation y de tipo de parámetro (`MethodArgumentNotValidException`,
  `MethodArgumentTypeMismatchException`).
- `endpoint` contiene la URI de la petición; `timestamp` es un `Instant` en UTC.
- Solo las excepciones con manejador en `GlobalExceptionHandler` (y los errores de seguridad) usan este contrato. Esto
  incluye ruta inexistente (404), método no soportado (405) y tipo de contenido no soportado (415). No hay un manejador
  genérico de `Exception`: capturaría también `AccessDeniedException` de `@PreAuthorize` y convertiría los 403 en 500. Por
  eso, cualquier otro error inesperado termina en HTTP 500 con el formato por defecto de Spring. Para que otra excepción
  de Spring MVC use el contrato, hay que añadirle un manejador explícito.

## 2. Códigos de error

| HTTP | Código `error`             | Excepción / origen                                                          | Cuándo ocurre                                                                    |
|------|----------------------------|-----------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| 400  | `VALIDATION_ERROR`         | `MethodArgumentNotValidException`                                           | Falla una anotación de Bean Validation en el body (incluye `details` por campo)  |
| 400  | `VALIDATION_ERROR`         | `MethodArgumentTypeMismatchException`                                       | Parámetro de URL o path con formato inválido (fecha, número, enum `tipo`/`granularity`) |
| 400  | `VALIDATION_ERROR`         | `HttpMessageNotReadableException`                                           | Body JSON malformado o ilegible                                                  |
| 400  | `VALIDATION_ERROR`         | `IllegalArgumentException`                                                  | Regla de negocio incumplida en un servicio                                       |
| 400  | `INVALID_ROLE`             | `InvalidRoleException`                                                      | Rol inexistente o intento de registrar `ADMIN`                                   |
| 401  | `INVALID_CREDENTIALS`      | `InvalidCredentialsException`                                               | Usuario o contraseña incorrectos en `/api/auth/login`                            |
| 401  | `UNAUTHORIZED`             | `TokenAuthenticationEntryPoint`                                             | Petición a endpoint protegido sin cabecera `Authorization: Bearer`               |
| 401  | `TOKEN_EXPIRED`            | `JwtAuthFilter` (`ExpiredJwtException`)                                     | Token expirado                                                                   |
| 401  | `TOKEN_INVALID`            | `JwtAuthFilter` (`JwtException`, `IllegalArgumentException`, usuario no encontrado) | Token malformado, firma inválida o usuario del `sub` inexistente         |
| 401  | `ACCOUNT_DISABLED`         | `JwtAuthFilter` (`usuario.isEnabled() == false`)                            | Usuario inactivo con token válido                                                |
| 403  | `FORBIDDEN`                | `accessDeniedHandler` de `SecurityConfig`                                   | Rol sin permiso (regla de `SecurityConfig` o `@PreAuthorize`)                    |
| 404  | `NOT_FOUND`                | `ProductoNotFoundException`                                                 | Producto inexistente (o inactivo, en operaciones que exigen producto activo)     |
| 404  | `NOT_FOUND`                | `NoResourceFoundException`                                                  | Ruta inexistente (con token válido; sin token responde 401)                      |
| 409  | `USER_ALREADY_EXISTS`      | `UserAlreadyExistsException`                                                | Username ya registrado                                                           |
| 409  | `DUPLICATE_RESOURCE`       | `DuplicateResourceException`                                                | Producto con `codigo` repetido, categoría con nombre repetido (sin distinguir mayúsculas) o proveedor con RUC repetido |
| 409  | `DATA_INTEGRITY_VIOLATION` | `DataIntegrityViolationException`                                           | Violación de restricción de base de datos                                        |
| 409  | `STOCK_INSUFICIENTE`       | `StockInsuficienteException`                                                | Consumo o descuento que dejaría el stock negativo                                |
| 405  | `METHOD_NOT_ALLOWED`       | `HttpRequestMethodNotSupportedException`                                    | Método HTTP no soportado para la ruta (incluye cabecera `Allow`)                 |
| 415  | `UNSUPPORTED_MEDIA_TYPE`   | `HttpMediaTypeNotSupportedException`                                        | `Content-Type` no soportado en un endpoint con body (incluye cabecera `Accept`)  |
| 500  | —                          | Cualquier otra excepción                                                    | Error interno no previsto (sin contrato propio)                                  |

Notas:

- Un `ProductoNotFoundException` con producto inactivo se lanza en `entrada`, `ajuste` (búsqueda con `activo = true`).
  En `GET /api/existencias` un producto inactivo devuelve `400 VALIDATION_ERROR`, no 404.
- En login, cualquier `AuthenticationException` de Spring (incluida cuenta deshabilitada) se convierte en
  `401 INVALID_CREDENTIALS`. `ACCOUNT_DISABLED` solo se emite desde el filtro JWT.
- Los errores de seguridad (401 y 403) los escribe `ErrorResponseWriter` con el mismo contrato.
- Si dos peticiones concurrentes crean el mismo código de producto, la restricción única de base de datos puede saltar
  antes que la validación del servicio y la respuesta sería `409 DATA_INTEGRITY_VIOLATION` en lugar de
  `409 DUPLICATE_RESOURCE`.

## 3. Producto, categoría y proveedor: cuándo es 400, 404 o 409

La misma situación se reporta distinto según el endpoint. Esta tabla coincide con `CATALOGO_ENDPOINTS.md` y con
`CATALOGO_REGLAS_NEGOCIO.md` (RN101, RN102, RN105, RN202, RN206, RN209, M200-03).

| Situación                                        | Endpoint                                                                                                  | Respuesta                 |
|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------|---------------------------|
| Producto inexistente                             | `PUT /api/productos/{id}`, `PUT /api/productos/{id}/stock-minimo`, `PATCH /api/productos/{id}/desactivar` | 404 `NOT_FOUND`           |
| Producto inexistente o inactivo                  | `POST /api/movimientos/entrada`, `POST /api/movimientos/ajuste`                                           | 404 `NOT_FOUND`           |
| Producto inexistente                             | `GET /api/existencias`                                                                                    | 404 `NOT_FOUND`           |
| Producto inactivo                                | `GET /api/existencias`                                                                                    | 400 `VALIDATION_ERROR`    |
| Producto inexistente o inactivo                  | `POST /api/consumos`                                                                                      | 400 `VALIDATION_ERROR`    |
| Categoría o proveedor inexistente o inactivo     | `POST /api/productos`, `PUT /api/productos/{id}`                                                          | 400 `VALIDATION_ERROR`    |
| Proveedor inexistente o inactivo                 | `POST /api/movimientos/entrada`                                                                           | 400 `VALIDATION_ERROR`    |
| Código de producto duplicado                     | `POST /api/productos`, `PUT /api/productos/{id}`                                                          | 409 `DUPLICATE_RESOURCE`  |
| Nombre de categoría o RUC de proveedor duplicado | `POST /api/categorias`, `POST /api/proveedores`                                                           | 409 `DUPLICATE_RESOURCE`  |
| Username duplicado                               | `POST /api/auth/register`                                                                                 | 409 `USER_ALREADY_EXISTS` |

Los tres endpoints de producto por `id` (`PUT`, `PUT stock-minimo`, `PATCH desactivar`) buscan el producto sin filtrar
por `activo`, por eso un producto inactivo no devuelve 404 en ellos.
