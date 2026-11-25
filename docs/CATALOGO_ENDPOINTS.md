# Catálogo de endpoints — Sistema de Gestión de Inventario (Taller Mecánico)

Documento generado a partir del código actual: controladores, DTOs, `SecurityConfig`, anotaciones `@PreAuthorize` y
`GlobalExceptionHandler`.

Documentos relacionados: [REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md), [VALIDACIONES.md](VALIDACIONES.md),
[POLITICAS_DE_SEGURIDAD.md](POLITICAS_DE_SEGURIDAD.md), [CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md),
y [REQUISITOS_FUNCIONALES.md](REQUISITOS_FUNCIONALES.md).

- **Prefijo global:** `/api`
- **Convención de rutas:** los dominios de escritura exponen una raíz plana por entidad (`/api/productos`,
  `/api/movimientos`, `/api/consumos`); los dominios de consulta agrupan sus recursos bajo un namespace
  (`/api/reportes/*`, `/api/monitoreo/{alertas,existencias}`).
- **Base URL:** `http://<host>:${SERVER_PORT}`
- **Formato:** `application/json`
- **Autenticación:** JWT en cabecera `Authorization: Bearer <token>`
- **Fechas:** `Instant` en UTC con formato `yyyy-MM-dd'T'HH:mm:ss'Z'`; `LocalDate` con formato `yyyy-MM-dd`

## 1. Roles

| Rol           | Descripción                                                                                                                                            |
|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ADMIN`       | Usuario semilla. Único rol que puede registrar usuarios. No puede asignarse mediante `/api/auth/register`.                                             |
| `JEFE_TALLER` | Administra catálogo, productos, entradas y ajustes de inventario.                                                                                      |
| `MECANICO`    | Registra consumos y consulta productos, existencias y alertas. No accede a dashboard, reportes ni estadísticas. No modifica catálogo ni stock directo. |

## 2. Resumen de endpoints y matriz de acceso

| Dominio         | Método | Ruta                                 | ADMIN | JEFE_TALLER | MECANICO | Público |
|-----------------|--------|--------------------------------------|:-----:|:-----------:|:--------:|:-------:|
| Seguridad       | POST   | `/api/auth/register`                 |  ✅   |     ❌      |    ❌    |    —    |
| Seguridad       | POST   | `/api/auth/login`                    |   —   |      —      |    —     |   ✅    |
| Seguridad       | GET    | `/api/usuarios`                      |  ✅   |     ❌      |    ❌    |    —    |
| Seguridad       | GET    | `/api/usuarios/{id}`                 |  ✅   |     ❌      |    ❌    |    —    |
| Seguridad       | PUT    | `/api/usuarios/{id}`                 |  ✅   |     ❌      |    ❌    |    —    |
| Seguridad       | PATCH  | `/api/usuarios/{id}/password`        |  ✅   |     ❌      |    ❌    |    —    |
| Auditoría       | GET    | `/api/auditoria`                     |  ✅   |     ❌      |    ❌    |    —    |
| Catálogo        | POST   | `/api/categorias`                    |  ❌   |     ✅      |    ❌    |    —    |
| Catálogo        | GET    | `/api/categorias`                    |  ❌   |     ✅      |    ❌    |    —    |
| Catálogo        | GET    | `/api/categorias/activos`            |  ❌   |     ✅      |    ✅    |    —    |
| Catálogo        | GET    | `/api/categorias/{id}/unidades`      |  ❌   |     ✅      |    ✅    |    —    |
| Unidades        | GET    | `/api/unidades`                      |  ✅   |     ✅      |    ✅    |    —    |
| Catálogo        | POST   | `/api/proveedores`                   |  ❌   |     ✅      |    ❌    |    —    |
| Catálogo        | GET    | `/api/proveedores`                   |  ❌   |     ✅      |    ❌    |    —    |
| Catálogo        | GET    | `/api/proveedores/activos`           |  ❌   |     ✅      |    ✅    |    —    |
| Productos       | POST   | `/api/productos`                     |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | GET    | `/api/productos`                     |  ❌   |     ✅      |    ✅    |    —    |
| Productos       | PUT    | `/api/productos/{id}`                |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | PUT    | `/api/productos/{id}/stock-minimo`   |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | PATCH  | `/api/productos/{id}/desactivar`     |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | PATCH  | `/api/productos/{id}/activar`        |  ❌   |     ✅      |    ❌    |    —    |
| Monitoreo       | GET    | `/api/monitoreo/existencias`                   |  ❌   |     ✅      |    ✅    |    —    |
| Inventario      | POST   | `/api/movimientos/entrada`                      |  ❌   |     ✅      |    ❌    |    —    |
| Inventario      | POST   | `/api/movimientos/ajuste`                       |  ❌   |     ✅      |    ❌    |    —    |
| Inventario      | POST   | `/api/consumos`                      |  ❌   |     ✅      |    ✅    |    —    |
| Inventario      | GET    | `/api/consumos/productos-frecuentes` |  ❌   |     ✅      |    ✅    |    —    |
| Monitoreo       | GET    | `/api/monitoreo/alertas`                       |  ❌   |     ✅      |    ✅    |    —    |
| Dashboard       | GET    | `/api/dashboard`                     |  ❌   |     ✅      |    ❌    |    —    |
| Inventario      | GET    | `/api/movimientos`                   |  ❌   |     ✅      |    ❌    |    —    |
| Reportes        | GET    | `/api/reportes/valor-inventario`     |  ❌   |     ✅      |    ❌    |    —    |
| Exportación     | GET    | `/api/reportes/valor-inventario/exportar` | ❌ |    ✅      |    ❌    |    —    |
| Estadísticas    | GET    | `/api/estadisticas`                  |  ❌   |     ✅      |    ❌    |    —    |
| Estadísticas    | GET    | `/api/estadisticas/por-tipo`         |  ❌   |     ✅      |    ❌    |    —    |
| Infraestructura | GET    | `/actuator/health`                   |   —   |      —      |    —     |   ✅    |
| Infraestructura | GET    | `/actuator/info`                     |   —   |      —      |    —     |   ✅    |

---

## 3. Dominio: Seguridad

### 3.1 POST `/api/auth/register`

Registra un usuario con rol `JEFE_TALLER` o `MECANICO`. El rol `ADMIN` está prohibido.

- **Acceso:** `ADMIN`
- **Body:**

| Campo      | Tipo   | Obligatorio | Validación                                                               |
|------------|--------|-------------|--------------------------------------------------------------------------|
| `username` | string | Sí          | 3–20 caracteres; solo letras, números y `_`                              |
| `password` | string | Sí          | Mínimo 8 caracteres; al menos una mayúscula y un número                  |
| `role`     | string | Sí          | Solo letras mayúsculas y `_`; valores válidos: `JEFE_TALLER`, `MECANICO` |

```json
{
  "username": "jefe_taller1",
  "password": "Password123",
  "role": "JEFE_TALLER"
}
```

- **Respuesta 201 Created:**

```json
{
  "id": 5,
  "username": "jefe_taller1",
  "role": "JEFE_TALLER",
  "createdAt": "2026-10-31T15:04:05Z"
}
```

- **Errores:** `400 VALIDATION_ERROR`, `400 INVALID_ROLE`, `401` (sin token), `403 FORBIDDEN`,`409 USER_ALREADY_EXISTS`.

---

### 3.2 POST `/api/auth/login`

Autentica un usuario y emite un JWT.

- **Acceso:** Público
- **Body:**

| Campo      | Tipo   | Obligatorio | Validación                     |
|------------|--------|-------------|--------------------------------|
| `username` | string | Sí          | No vacío, máximo 50 caracteres |
| `password` | string | Sí          | No vacío                       |

```json
{
  "username": "jefe_taller1",
  "password": "Password123"
}
```

- **Respuesta 200 OK:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 5,
  "role": "JEFE_TALLER"
}
```

`expiresIn` se expresa en segundos y proviene de `JWT_EXPIRACION_SEGUNDOS`.

- **Errores:** `400 VALIDATION_ERROR`, `401 INVALID_CREDENTIALS`, `429 RATE_LIMIT_EXCEEDED` (con `Retry-After`).

---

### 3.3 Mantenimiento de usuarios

Todos los endpoints de esta sección requieren `ADMIN`. Las respuestas nunca incluyen el hash de la contraseña.

#### GET `/api/usuarios`

Lista paginada. Parámetros opcionales: `page` (default `0`), `size` (default `10`, máximo `100`), `username` (búsqueda parcial), `role` (`ADMIN`, `JEFE_TALLER` o `MECANICO`) y `activo` (`true`/`false`).

Respuesta 200 con `PaginaResponse<UsuarioResponse>`:

```json
{
  "content": [
    {
      "id": 5,
      "username": "jefe_taller1",
      "role": "JEFE_TALLER",
      "activo": true,
      "createdAt": "2026-10-31T15:04:05Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

- **Errores:** `400 VALIDATION_ERROR`, `400 INVALID_ROLE` (filtro `role` inválido), `401`, `403 FORBIDDEN`.

#### GET `/api/usuarios/{id}`

Devuelve el mismo `UsuarioResponse`. Errores: `404 NOT_FOUND`, `401`, `403 FORBIDDEN`.

#### PUT `/api/usuarios/{id}`

Cambia el rol y el estado activo. No existe borrado físico: enviar `activo: false` desactiva la cuenta.

```json
{ "role": "MECANICO", "activo": false }
```

No se puede desactivar ni cambiar de rol al último `ADMIN` activo: responde `409 ULTIMO_ADMIN_ACTIVO`. Los cambios de rol y estado se reflejan en la siguiente petición porque `JwtAuthFilter` recarga al usuario desde la base de datos en cada request.

- **Errores:** `400 VALIDATION_ERROR`, `400 INVALID_ROLE`, `401`, `403 FORBIDDEN`, `404 NOT_FOUND`, `409 ULTIMO_ADMIN_ACTIVO`.

#### PATCH `/api/usuarios/{id}/password`

Restablece la contraseña. La nueva contraseña debe tener al menos 8 caracteres, una mayúscula y un número. Responde el `UsuarioResponse` actualizado.

```json
{ "password": "NuevaPass123" }
```

- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `404 NOT_FOUND`.

### 3.4 GET `/api/auditoria`

Consulta paginada y solo lectura de los asientos de auditoría. Requiere `ADMIN`.

Parámetros opcionales:

- `page` (default `0`) y `size` (default `10`, máximo `100`).
- `desde` y `hasta` (`yyyy-MM-dd`): rango inclusivo interpretado en `America/Lima` y convertido a UTC.
- `actor`: búsqueda parcial por el username guardado en el asiento.
- `tipo`: `OPERACION` o `AUTENTICACION`.
- `resultado`: `OK`, `ERROR` o un código concreto, por ejemplo `VALIDATION_ERROR`, `FORBIDDEN` o
  `STOCK_INSUFICIENTE`.

Respuesta 200 con `PaginaResponse<AuditoriaResponse>`:

```json
{
  "content": [
    {
      "id": 12,
      "fecha": "2026-11-19T17:04:05Z",
      "tipo": "OPERACION",
      "actorId": 5,
      "actorUsername": "jefe_taller1",
      "metodo": "PUT",
      "endpoint": "/api/productos/8",
      "recurso": "producto",
      "recursoId": "8",
      "resultado": "OK",
      "exito": true
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

El actor se guarda como snapshot (`actorId` y `actorUsername`), por lo que el historial conserva el nombre correcto
aunque el usuario sea modificado o desactivado después. Las operaciones de autenticación se registran con
`tipo: "AUTENTICACION"` y, cuando no hay usuario autenticado, `actorUsername: "sistema"`. No se persisten passwords,
tokens ni cuerpos de petición.

- **Errores:** `400 VALIDATION_ERROR` (rango invertido, `tipo` inválido), `401`, `403 FORBIDDEN`.

### 3.5 Limitación de frecuencia

Todas las rutas `/api/**` pueden responder `429 RATE_LIMIT_EXCEEDED` con la cabecera `Retry-After` en segundos. El
cuerpo usa el contrato de `docs/TABLA_ERRORES.md`. Los rechazos se publican como eventos de seguridad y se registran en
el log, no en la auditoría de operaciones.

| Ámbito | Clave | Límite | Variable de entorno |
|--------|-------|--------|---------------------|
| `POST /api/auth/login` | IP de origen (con `X-Forwarded-For` o `X-Real-IP` si existen) | Por ventana | `RATE_LIMIT_LOGIN_IP` |
| `POST /api/auth/login` | Username normalizado | Por ventana | `RATE_LIMIT_LOGIN_USERNAME` |
| Resto de endpoints autenticados | `id` del usuario de la BD | Por ventana | `RATE_LIMIT_USUARIO` |
| Swagger (`/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-resources/**`, `/webjars/swagger-ui/**`) | `id` del usuario de la BD | Límite holgado | `RATE_LIMIT_SWAGGER` |

Las ventanas se configuran con `RATE_LIMIT_VENTANA_LOGIN_SEGUNDOS` y `RATE_LIMIT_VENTANA_USUARIO_SEGUNDOS`.
`/actuator/health` y `/error` están excluidos. El filtro de login corre antes de `JwtAuthFilter` y el de usuario
después, por lo que un cambio de rol o de estado en la BD se refleja antes de aplicar el límite por cuenta.

El estado se mantiene en memoria (`InMemoryRateLimitStore`) detrás de la interfaz `RateLimitStore`, de modo que puede
sustituirse por un store compartido si aparecen más réplicas. No hay valores por defecto en `application.yaml`: todas
las variables están listadas en `.env.example`.

---

## 4. Dominio: Catálogo (categorías y proveedores)

### 4.1 POST `/api/categorias`

Crea una categoría. El nombre es obligatorio y único sin distinguir mayúsculas/minúsculas. El usuario no elige
unidades: las categorías base y sus unidades válidas las siembra la migración V10, y toda categoría nueva se crea con
la unidad `UND` (RN503). El producto solo podrá usar unidades asociadas a su categoría.

- **Acceso:** `JEFE_TALLER`
- **Body:**

| Campo         | Tipo    | Obligatorio | Validación                                    |
|---------------|---------|-------------|-----------------------------------------------|
| `nombre`      | string  | Sí          | No vacío, máximo 100 caracteres               |
| `descripcion` | string  | No          | Máximo 255 caracteres                         |

```json
{
  "nombre": "Cables",
  "descripcion": "Cables y conectores"
}
```

- **Respuesta 201 Created:**

```json
{
  "id": 21,
  "nombre": "Cables",
  "descripcion": "Cables y conectores",
  "activo": true,
  "unidades": [
    { "id": 1, "codigo": "UND", "nombre": "Unidad", "permiteFragmentacion": false }
  ]
}
```

- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `409 DUPLICATE_RESOURCE`.

---

### 4.2 POST `/api/proveedores`

Crea un proveedor. El RUC es obligatorio y único.

- **Acceso:** `JEFE_TALLER`
- **Body:**

| Campo         | Tipo   | Obligatorio | Validación                              |
|---------------|--------|-------------|-----------------------------------------|
| `ruc`         | string | Sí          | No vacío, máximo 20 caracteres          |
| `razonSocial` | string | Sí          | No vacío, máximo 150 caracteres         |
| `contacto`    | string | No          | Máximo 100 caracteres                   |
| `telefono`    | string | No          | Máximo 30 caracteres                    |
| `correo`      | string | No          | Formato de email, máximo 150 caracteres |
| `direccion`   | string | No          | Máximo 255 caracteres                   |

```json
{
  "ruc": "1792345678001",
  "razonSocial": "Repuestos Andinos S.A.",
  "contacto": "María López",
  "telefono": "0998765432",
  "correo": "ventas@repuestosandinos.com",
  "direccion": "Av. Amazonas 123"
}
```

- **Respuesta 201 Created:**

```json
{
  "id": 7,
  "ruc": "1792345678001",
  "razonSocial": "Repuestos Andinos S.A.",
  "contacto": "María López",
  "telefono": "0998765432",
  "correo": "ventas@repuestosandinos.com",
  "direccion": "Av. Amazonas 123",
  "activo": true
}
```

- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `409 DUPLICATE_RESOURCE`.

### 4.3 GET `/api/categorias` y GET `/api/proveedores`

Listados paginados del catálogo, con filtros por nombre/razón social y por `activo`. Incluyen registros inactivos
cuando no se aplica el filtro. Acceso: `JEFE_TALLER`.

### 4.4 GET `/api/categorias/activos` y GET `/api/proveedores/activos`

Listados ligeros y sin paginar de solo los registros activos, pensados para los combobox de filtros. Acceso:
`JEFE_TALLER` y `MECANICO`. Devuelven `List<CategoriaResponse>` / `List<ProveedorResponse>`.

> No existen endpoints para actualizar ni desactivar categorías/proveedores.

---

## 5. Dominio: Productos

### 5.1 POST `/api/productos`

Crea un producto. El stock inicial siempre es `0`. El backend genera el `codigo` con el patrón `AAA-nnnn`: `AAA` son
las tres primeras letras del nombre de la categoría y `nnnn` un correlativo por prefijo (por ejemplo `FIL-0001` para
la categoría `Filtros`). El cliente no lo envía ni lo edita.

- **Acceso:** `JEFE_TALLER`
- **Body:**

| Campo          | Tipo    | Obligatorio | Validación                                      |
|----------------|---------|-------------|-------------------------------------------------|
| `nombre`       | string  | Sí          | No vacío, máximo 150 caracteres                 |
| `descripcion`  | string  | No          | Máximo 500 caracteres                           |
| `unidadBaseId` | integer | Sí          | `> 0`; unidad activa y válida para la categoría |
| `unidadPresentacionId` | integer | No   | Presentación (`permiteFragmentacion = true`) válida para la categoría; si se omite, no hay presentación |
| `factorPresentacion` | integer | Si hay presentación | `> 0`; unidades base por presentación. Se omite si no hay presentación |
| `stockMinimo`  | integer | Sí          | `>= 0`                                          |
| `id_categoria` | integer | Sí          | `> 0`; la categoría debe existir y estar activa |
| `id_proveedor` | integer | Sí          | `> 0`; el proveedor debe existir y estar activo |

Los campos `id_categoria` e `id_proveedor` aceptan como alias `idCategoria`/`categoriaId` y `idProveedor`/`proveedorId`.

```json
{
  "nombre": "Filtro de aceite",
  "descripcion": "Filtro para motor 1.6L",
  "unidadBaseId": 1,
  "stockMinimo": 10,
  "id_categoria": 3,
  "id_proveedor": 7
}
```

- **Respuesta 201 Created:** objeto `ProductoResponse` (ver sección 5.6).
- **Errores:** `400 VALIDATION_ERROR` (incluye categoría/proveedor inexistente o inactivo), `401`, `403 FORBIDDEN`.

---

### 5.2 GET `/api/productos`

Lista productos con paginación y filtros opcionales.

- **Acceso:** `JEFE_TALLER`, `MECANICO`
- **Parámetros de URL:**

| Parámetro      | Tipo    | Obligatorio | Default | Descripción                                                |
|----------------|---------|-------------|---------|------------------------------------------------------------|
| `page`         | integer | No          | `0`     | Número de página (se normaliza a `>= 0`)                   |
| `size`         | integer | No          | `10`    | Tamaño de página (se limita a 1–100)                       |
| `nombre`       | string  | No          | —       | Búsqueda parcial por nombre, sin distinguir mayúsculas     |
| `categoria`    | integer | No          | —       | Filtra por ID de categoría                                 |
| `codigo`       | string  | No          | —       | Búsqueda parcial por código, sin distinguir mayúsculas     |
| `proveedor`    | integer | No          | —       | Filtra por ID de proveedor                                 |
| `unidadBaseId` | integer | No          | —       | Filtra por ID de unidad base                               |
| `activo`       | boolean | No          | —       | Filtra por estado activo                                   |
| `stockCritico` | boolean | No          | —       | `true` si `stockActual <= stockMinimo`; `false` en caso contrario |

- **Ejemplo:** `GET /api/productos?page=0&size=10&nombre=filtro&categoria=3&stockCritico=true`
- **Respuesta 200 OK:** `content` (lista de `ProductoResponse`) más cuatro campos de paginación.

```json
{
  "content": [
    {
      "id": 12,
      "codigo": "FIL-0001",
      "nombre": "Filtro de aceite",
      "descripcion": "Filtro para motor 1.6L",
      "unidadBaseId": 1,
      "stockActual": 25,
      "stockMinimo": 10,
      "stockCritico": false,
      "costoUltimaCompra": 8.50,
      "activo": true,
      "categoriaId": 3,
      "categoriaNombre": "Filtros",
      "proveedorId": 7,
      "proveedorRazonSocial": "Repuestos Andinos S.A."
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

| Campo           | Tipo    | Descripción                                  |
|-----------------|---------|----------------------------------------------|
| `page`          | integer | Número de la página actual (empieza en `0`)  |
| `size`          | integer | Tamaño de página aplicado                    |
| `totalElements` | integer | Total de registros que cumplen el filtro     |
| `totalPages`    | integer | Total de páginas                             |

Orden por defecto: `nombre` ascendente y luego `id`. El listado incluye también productos inactivos.

- **Errores:** `400 VALIDATION_ERROR` (parámetro con formato inválido), `401`, `403 FORBIDDEN`.

---

### 5.3 PUT `/api/productos/{id}`

Actualiza un producto completo.

- **Acceso:** `JEFE_TALLER`
- **Path:** `id` (integer, obligatorio)
- **Body:**

| Campo          | Tipo    | Obligatorio | Validación                            |
|----------------|---------|-------------|---------------------------------------|
| `nombre`       | string  | Sí          | No vacío, máximo 150                  |
| `descripcion`  | string  | No          | Máximo 500                            |
| `unidadBaseId` | integer | Sí          | `> 0`, existente y válida para la categoría |
| `unidadPresentacionId` | integer | No   | Presentación (`permiteFragmentacion = true`) válida para la categoría; si se omite, se quita la presentación |
| `factorPresentacion` | integer | Si hay presentación | `> 0`; unidades base por presentación. Se omite si no hay presentación |
| `stockMinimo`  | integer | Sí          | `>= 0`                                |
| `activo`       | boolean | No          | Si se omite, conserva el valor actual |
| `id_categoria` | integer | Sí          | `> 0`, existente y activa             |
| `id_proveedor` | integer | Sí          | `> 0`, existente y activo             |

- **Respuesta 200 OK:** `ProductoResponse`.
- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `404 NOT_FOUND`.

> El `codigo` no se envía ni se edita: lo genera el backend y permanece inmutable.
> Si el producto ya tiene movimientos registrados, la unidad base es inmutable; solo se pueden cambiar `nombre`,
> `descripcion`, presentación y factor. `stockMinimo`, `activo`, categoría y proveedor deben enviarse sin cambios
> (si no, `400`).

---

### 5.4 PUT `/api/productos/{id}/stock-minimo`

Actualiza únicamente el stock mínimo.

- **Acceso:** `JEFE_TALLER`
- **Path:** `id` (integer, obligatorio)
- **Body:**

```json
{
  "nuevoStockMinimo": 15
}
```

| Campo              | Tipo    | Obligatorio | Validación |
|--------------------|---------|-------------|------------|
| `nuevoStockMinimo` | integer | Sí          | `>= 0`     |

- **Respuesta 200 OK:** `ProductoResponse`.
- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `404 NOT_FOUND`.

> Esta operación no verifica si el producto tiene movimientos: el stock mínimo se actualiza directamente.

---

### 5.5 PATCH `/api/productos/{id}/desactivar`

Desactiva lógicamente un producto (`activo = false`).

- **Acceso:** `JEFE_TALLER`
- **Path:** `id` (integer, obligatorio)
- **Body:** ninguno.
- **Respuesta 200 OK:** `ProductoResponse` con `activo: false`.
- **Errores:** `401`, `403 FORBIDDEN`, `404 NOT_FOUND`.

> La desactivación lógica se puede aplicar siempre, incluso si el producto tiene movimientos registrados (RN108).

---

### 5.5.1 PATCH `/api/productos/{id}/activar`

Reactiva un producto desactivado (`activo = true`). Es idempotente: si ya está activo, responde 200 sin cambios.

- **Acceso:** `JEFE_TALLER`
- **Path:** `id` (integer, obligatorio)
- **Body:** ninguno.
- **Respuesta 200 OK:** `ProductoResponse` con `activo: true`.
- **Errores:** `400` si la categoría o el proveedor del producto están inactivos, `401`, `403 FORBIDDEN`, `404 NOT_FOUND`.

---

### 5.6 Estructura `ProductoResponse`

| Campo                  | Tipo    | Descripción                             |
|------------------------|---------|-----------------------------------------|
| `id`                   | integer | Identificador                           |
| `codigo`               | string  | Código generado (`AAA-nnnn`), inmutable |
| `nombre`               | string  | Nombre                                  |
| `descripcion`          | string  | Descripción                             |
| `unidadBaseCodigo`     | string  | Unidad base (`UND`, `GALON`, `BOTELLA`, ...); ver también `unidadBaseId`/`unidadBaseNombre` |
| `stockActual`          | integer | Stock actual                            |
| `stockMinimo`          | integer | Stock mínimo configurado                |
| `stockCritico`         | boolean | `stockActual <= stockMinimo`            |
| `costoUltimaCompra`    | number  | Último costo unitario; puede ser `null` |
| `activo`               | boolean | Estado lógico                           |
| `categoriaId`          | integer | ID de categoría                         |
| `categoriaNombre`      | string  | Nombre de categoría                     |
| `proveedorId`          | integer | ID de proveedor                         |
| `proveedorRazonSocial` | string  | Razón social del proveedor              |

---

## 6. Dominio: Inventario (movimientos)

> La consulta de existencias se mantiene aquí por continuidad del documento, pero pertenece al dominio `monitoreo`
> junto con las alertas (§7); ambos comparten la regla `EstadoStock`.

### 6.1 GET `/api/monitoreo/existencias`

Listado operativo del stock. **Solo incluye productos activos**; no admite parámetro `activo`, a diferencia de
`GET /api/productos`.

- **Acceso:** `JEFE_TALLER`, `MECANICO`
- **Parámetros de URL:**

| Parámetro     | Tipo    | Obligatorio | Default | Descripción                                              |
|---------------|---------|-------------|---------|----------------------------------------------------------|
| `page`        | integer | No          | `0`     | Número de página (se normaliza a `>= 0`)                 |
| `size`        | integer | No          | `10`    | Tamaño de página (se limita a 1–100)                     |
| `nombre`      | string  | No          | —       | Búsqueda parcial por nombre o código                     |
| `categoria`   | integer | No          | —       | Filtra por ID de categoría                               |
| `proveedorId` | integer | No          | —       | Filtra por ID de proveedor                               |
| `estadoStock` | string  | No          | —       | `NORMAL`, `BAJO` o `SIN_STOCK`                           |

- **Ejemplo:** `GET /api/monitoreo/existencias?categoria=3&estadoStock=BAJO&page=0&size=10`
- **Respuesta 200 OK:** `PaginaResponse<ExistenciaConsultaResponse>` con los campos del producto más `estadoStock`
  (`NORMAL`, `BAJO`, `SIN_STOCK`) y `cantidadSugerida`. La clasificación y la sugerencia usan la misma regla que
  `GET /api/monitoreo/alertas`.

```json
{
  "content": [
    {
      "id": 12,
      "codigo": "FIL-0001",
      "nombre": "Filtro de aceite",
      "categoriaNombre": "Filtros",
      "proveedorRazonSocial": "Repuestos Andinos S.A.",
      "stockActual": 4,
      "stockMinimo": 10,
      "stockCritico": true,
      "estadoStock": "BAJO",
      "cantidadSugerida": 6
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

- **Errores:** `400 VALIDATION_ERROR` (parámetros con formato inválido), `401`, `403 FORBIDDEN`.

---

### 6.2 POST `/api/movimientos/entrada`

Registra una entrada de stock, incrementa `stockActual` y actualiza `costoUltimaCompra`. El usuario y la fecha se toman
del token.

- **Acceso:** `JEFE_TALLER`
- **Body:**

| Campo           | Tipo    | Obligatorio | Validación                             |
|-----------------|---------|-------------|----------------------------------------|
| `productoId`    | integer | Sí          | Producto existente y activo            |
| `proveedorId`   | integer | Sí          | Proveedor existente y activo           |
| `cantidad`      | integer | Sí          | `> 0`                                  |
| `costoUnitario` | number  | Sí          | `>= 0`, hasta 10 enteros y 2 decimales |

```json
{
  "productoId": 12,
  "proveedorId": 7,
  "cantidad": 20,
  "costoUnitario": 8.50
}
```

- **Respuesta 201 Created:**

```json
{
  "movimiento": {
    "id": 101,
    "fecha": "2026-10-31T15:10:00Z",
    "tipo": "ENTRADA",
    "cantidad": 20,
    "variacion": 20,
    "stockAnterior": 5,
    "stockPosterior": 25,
    "costoUnitario": 8.50,
    "motivo": "Entrada de proveedor Repuestos Andinos S.A.",
    "productoId": 12,
    "consumoId": null,
    "usuarioId": 2
  },
  "producto": {}
}
```

El campo `producto` contiene el `ProductoResponse` actualizado.

- **Errores:** `400 VALIDATION_ERROR` (proveedor inexistente/inactivo, desbordamiento de stock), `401`, `403 FORBIDDEN`,
  `404 NOT_FOUND` (producto inexistente/inactivo).

---

### 6.3 POST `/api/movimientos/ajuste`

Registra un ajuste de inventario fijando la cantidad física real. No modifica el costo de última compra.

- **Acceso:** `JEFE_TALLER`
- **Body:**

| Campo            | Tipo    | Obligatorio | Validación                      |
|------------------|---------|-------------|---------------------------------|
| `productoId`     | integer | Sí          | Producto existente y activo     |
| `cantidadFisica` | integer | Sí          | `>= 0`                          |
| `justificacion`  | string  | Sí          | No vacía, máximo 255 caracteres |

```json
{
  "productoId": 12,
  "cantidadFisica": 18,
  "justificacion": "Conteo físico mensual"
}
```

- **Respuesta 201 Created:** `MovimientoResponse` (ver estructura abajo).
- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `404 NOT_FOUND`.

---

### 6.4 Estructura `MovimientoResponse`

| Campo            | Tipo             | Descripción                                                          |
|------------------|------------------|----------------------------------------------------------------------|
| `id`             | integer          | Identificador del movimiento                                         |
| `fecha`          | string (Instant) | Fecha del movimiento                                                 |
| `tipo`           | string           | `ENTRADA`, `CONSUMO` o `AJUSTE`                                      |
| `cantidad`       | integer          | Cantidad del movimiento. Puede ser `null` en un `AJUSTE`             |
| `variacion`      | integer          | Diferencia aplicada al stock (en `ENTRADA` coincide con la cantidad) |
| `stockAnterior`  | integer          | Stock antes del movimiento                                           |
| `stockPosterior` | integer          | Stock después del movimiento                                         |
| `costoUnitario`  | number           | Costo unitario (solo entradas)                                       |
| `motivo`         | string           | Motivo o justificación                                               |
| `productoId`     | integer          | ID del producto                                                      |
| `consumoId`      | integer          | ID de consumo asociado, si aplica                                    |
| `usuarioId`      | integer          | ID del usuario que registró                                          |

Semántica de `cantidad` y `variacion` según el tipo (RN210):

- `ENTRADA`: `cantidad` y `variacion` contienen las unidades ingresadas (mismo valor).
- `CONSUMO`: `cantidad` contiene las unidades descontadas y `variacion` es `null`.
- `AJUSTE`: `cantidad` es `null` y `variacion` contiene la diferencia con signo respecto al stock anterior.

---

### 6.5 POST `/api/consumos`

Registra el consumo de uno o varios productos por parte del mecánico. Descuenta el stock de forma atómica: si algún
ítem falla, no se descuenta nada. Genera un movimiento `CONSUMO` por ítem. El usuario y la fecha se toman del token y
del reloj del servidor.

- **Acceso:** `JEFE_TALLER`, `MECANICO` (**ADMIN recibe 403**)
- **Body:**

| Campo                       | Tipo    | Obligatorio | Validación                                                 |
|-----------------------------|---------|-------------|------------------------------------------------------------|
| `finalidad`                 | string  | Sí          | No vacía, máximo 255 caracteres                            |
| `items`                     | lista   | Sí          | Al menos un elemento                                       |
| `items[].productoId`        | integer | Sí          | `> 0`; producto existente y activo                         |
| `items[].cantidad`          | integer | Sí          | `> 0`                                                      |
| `referenciaVehiculoCliente` | string  | No          | Máximo 150 caracteres; si viene en blanco se guarda `null` |

```json
{
  "finalidad": "Cambio de aceite",
  "items": [
    {
      "productoId": 12,
      "cantidad": 2
    },
    {
      "productoId": 15,
      "cantidad": 1
    }
  ],
  "referenciaVehiculoCliente": "ABC-123"
}
```

- **Respuesta 201 Created:**

```json
{
  "id": 31,
  "fecha": "2026-10-31T15:20:00Z",
  "finalidad": "Cambio de aceite",
  "referenciaVehiculoCliente": "ABC-123",
  "usuarioId": 4,
  "movimientos": [
    {
      "id": 102,
      "fecha": "2026-10-31T15:20:00Z",
      "tipo": "CONSUMO",
      "cantidad": 2,
      "variacion": null,
      "stockAnterior": 25,
      "stockPosterior": 23,
      "costoUnitario": null,
      "motivo": "Cambio de aceite",
      "productoId": 12,
      "consumoId": 31,
      "usuarioId": 4
    }
  ]
}
```

- **Errores:**
    - `400 VALIDATION_ERROR`: campos inválidos, lista vacía, producto inexistente o inactivo
      (`Producto ID <id> no encontrado o inactivo`).
    - `401`, `403 FORBIDDEN` (rol `ADMIN`).
    - `409 STOCK_INSUFICIENTE`:`Stock insuficiente para el producto ID <id> (<nombre>): solicitado <n>, disponible <m>`.

> Si un mismo producto se repite en varios ítems, se suman sus cantidades para validar el stock disponible. Si esa
> suma desborda el máximo entero admitido, la petición se rechaza con `400 VALIDATION_ERROR` (RN211).

---

### 6.6 GET `/api/consumos/productos-frecuentes`

Atajo para el formulario de registro de consumos: devuelve los **5** productos activos con stock disponible más
consumidos, de modo que el usuario pueda agregarlos sin buscarlos en el catálogo.

- **Acceso:** `MECANICO`, `JEFE_TALLER`
- **Parámetros:** ninguno.
- **Periodo:** últimos 90 días, calculados en `America/Lima` y convertidos a UTC. No es configurable.
- **Filtro:** se excluyen los productos con `stockActual = 0` (o nulo). El filtro se aplica antes del corte, por lo que
  si un producto frecuente está sin stock, lo ocupa el siguiente de la lista.
- **Orden:** por `vecesConsumido` descendente; si no hay historial, por nombre ascendente.
- **Respuesta 200 OK:** array simple (sin paginación) de `ProductoFrecuenteResponse`:

```json
[
  {
    "productoId": 12,
    "codigo": "FIL-0001",
    "nombre": "Abrazadera Aston Martin Thema",
    "unidadBaseId": 1,
    "stockActual": 89,
    "stockMinimo": 10,
    "stockCritico": false,
    "estadoStock": "NORMAL",
    "categoriaNombre": "Neumáticos",
    "vecesConsumido": 17,
    "unidadesConsumidas": 42
  }
]
```

- **Errores:** `401`, `403 FORBIDDEN`.

> Este listado es la vista por defecto del selector de repuestos, **no sustituye a la búsqueda**: si el producto
> consumido no está entre los cinco, el formulario debe seguir usando `GET /api/productos` para buscarlo por código
> o nombre. Solo se incluyen productos activos; elaborar un consumo con un producto inactivo sería rechazado con
> `400 VALIDATION_ERROR` por `POST /api/consumos`.

---

## 7. Dominio: Monitoreo (alertas)

### 7.1 GET `/api/monitoreo/alertas`

Lista los productos activos con bajo stock (`stockActual <= stockMinimo`), ordenados por cantidad sugerida descendente y
luego por nombre.

- **Acceso:** `JEFE_TALLER`, `MECANICO`
- **Parámetros:** ninguno.
- **Respuesta 200 OK:**

```json
[
  {
    "codigo": "FIL-0001",
    "nombre": "Filtro de aceite",
    "stockActual": 4,
    "stockMinimo": 10,
    "cantidadSugerida": 6,
    "proveedor": "Repuestos Andinos S.A."
  }
]
```

`cantidadSugerida = stockMinimo - stockActual`.

- **Errores:** `401`, `403 FORBIDDEN`.

---

## 8. Dominio: Dashboard

### 8.1 GET `/api/dashboard`

Devuelve el resumen general del inventario, KPIs del mes, alertas y últimos movimientos.

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)
- **Parámetros:** ninguno.
- **Respuesta 200 OK:**

```json
{
  "inventario": {
    "totalProductos": 40,
    "productosStockNormal": 34,
    "productosBajoStock": 6,
    "productosSinStock": 2
  },
  "kpis": {
    "valorEstimadoInventario": 12500.75,
    "productosSinCosto": 3,
    "entradasMes": 12,
    "consumosMes": 0,
    "ajustesMes": 2
  },
  "alertas": {
    "total": 6,
    "masUrgentes": [
      {
        "codigo": "FIL-0001",
        "nombre": "Filtro de aceite",
        "stockActual": 0,
        "stockMinimo": 10,
        "cantidadSugerida": 10,
        "proveedor": "Repuestos Andinos S.A."
      }
    ]
  },
  "ultimosMovimientos": [
    {
      "id": 101,
      "fecha": "2026-10-31T15:10:00Z",
      "tipo": "ENTRADA",
      "productoCodigo": "FIL-001",
      "productoNombre": "Filtro de aceite",
      "cantidad": 20,
      "variacion": 20,
      "usuario": "jefe_taller1"
    }
  ]
}
```

- `alertas.masUrgentes` se limita a los 5 primeros.
- `ultimosMovimientos` se limita a los 5 más recientes.
- Los KPIs de mes se calculan desde el día 1 del mes actual.

- **Errores:** `401`, `403 FORBIDDEN` (roles `ADMIN` y `MECANICO`).

---

## 9. Dominio: Reportes

> `GET /api/reportes/valor-inventario` devuelve el detalle paginado; su exportación entrega el contenido completo.
> El reporte de movimientos ya no pertenece a este dominio: está en Inventario (`GET /api/movimientos`).

### 9.1 GET `/api/movimientos`

> Perteneciente al dominio **Inventario**; se documenta aquí por continuidad del catálogo. Se sirve desde
> `MovimientoController`, que también expone `POST /api/movimientos/entrada` y `/ajuste`.

Reporte de movimientos en un rango de fechas, con totales por tipo y paginación.

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)
- **Parámetros de URL:**

| Parámetro | Tipo                | Obligatorio | Descripción                                      |
|-----------|---------------------|-------------|--------------------------------------------------|
| `desde`   | date (`yyyy-MM-dd`) | Sí*         | Fecha inicial inclusive                          |
| `hasta`   | date (`yyyy-MM-dd`) | Sí*         | Fecha final inclusive                            |
| `tipo`    | string              | No          | `ENTRADA`, `CONSUMO` o `AJUSTE`                  |
| `page`    | integer             | No          | Número de página, base 0 (se normaliza a `>= 0`) |
| `size`    | integer             | No          | Tamaño de página (se limita a 1–100)             |

\* En el controlador figuran como opcionales, pero el servicio exige ambos y devuelve `400` si falta alguno.

- **Ejemplo:** `GET /api/movimientos?desde=2026-10-01&hasta=2026-10-31&tipo=ENTRADA&page=0&size=10`
- **Respuesta 200 OK:**

```json
{
  "totalEntradas": 12,
  "totalConsumos": 0,
  "totalAjustes": 2,
  "movimientos": [
    {
      "id": 101,
      "fecha": "2026-10-31T15:10:00Z",
      "tipo": "ENTRADA",
      "cantidad": 20,
      "variacion": 20,
      "stockAnterior": 5,
      "stockPosterior": 25,
      "costoUnitario": 8.50,
      "motivo": "Entrada de proveedor Repuestos Andinos S.A.",
      "productoId": 12,
      "productoCodigo": "FIL-001",
      "productoNombre": "Filtro de aceite",
      "consumoId": null,
      "usuarioId": 2,
      "usuarioUsername": "jefe_taller1"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 14,
  "totalPages": 2
}
```

| Campo           | Tipo    | Descripción                                 |
|-----------------|---------|---------------------------------------------|
| `page`          | integer | Número de la página actual (empieza en `0`) |
| `size`          | integer | Tamaño de página aplicado                   |
| `totalElements` | integer | Total de movimientos que cumplen el filtro  |
| `totalPages`    | integer | Total de páginas                            |

- Cada movimiento incluye `productoCodigo`, `productoNombre` y `usuarioUsername` además de `productoId` y `usuarioId`,
  para que el reporte sea legible sin consultas adicionales.
- `size` por defecto es `10`, igual que en `GET /api/productos` (§5.2).
- Solo se pagina la lista `movimientos`. `totalEntradas`, `totalConsumos` y `totalAjustes` se calculan sobre **todos** los
  movimientos del filtro, no sobre la página, y no cambian al pasar de página.
- Orden: `fecha` descendente y luego `id` descendente.
- Una página fuera de rango devuelve `200 OK` con `movimientos` vacío y `totalElements` y `totalPages` correctos.

- **Errores:** `400 VALIDATION_ERROR` (faltan fechas, `hasta < desde`, formato inválido, `page` o `size` con formato
  inválido), `401`, `403 FORBIDDEN`.

---

### 9.2 GET `/api/reportes/valor-inventario`

Calcula el valor estimado del inventario activo: `stockActual * costoUltimaCompra` por producto.

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)
- **Parámetros de URL:**

| Parámetro | Tipo    | Obligatorio | Default | Descripción                     |
|-----------|---------|-------------|---------|---------------------------------|
| `page`    | integer | No          | `0`     | Número de página (se normaliza a `>= 0`) |
| `size`    | integer | No          | `10`    | Tamaño de página (se limita a 1–100) |

- **Respuesta 200 OK:** `ValorInventarioPaginaResponse`. Los totales cubren **todos** los productos activos, no solo la
  página; `detalle` contiene únicamente los registros de la página.

```json
{
  "totalGeneral": 12500.75,
  "totalProductosConCosto": 37,
  "productosSinCosto": 3,
  "detalle": [
    {
      "codigo": "FIL-0001",
      "nombre": "Filtro de aceite",
      "stockActual": 25,
      "costoUltimaCompra": 8.50,
      "valorEstimadoProducto": 212.50
    },
    {
      "codigo": "ACE-002",
      "nombre": "Aceite 5W-30",
      "stockActual": 10,
      "costoUltimaCompra": null,
      "valorEstimadoProducto": 0.00
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 40,
  "totalPages": 4
}
```

Los productos sin costo se incluyen en `detalle` con `costoUltimaCompra: null` y valor `0.00`, y no suman al total.
Una página fuera de rango devuelve `200 OK` con `detalle` vacío y los totales correctos.

- **Errores:** `400 VALIDATION_ERROR` (`page` o `size` con formato inválido), `401`, `403 FORBIDDEN`.

---

### 9.3 Exportación de valor de inventario (PDF y Excel)

Devuelve el contenido completo de `GET /api/reportes/valor-inventario` (todos los productos, sin paginar) como archivo
adjunto (`Content-Disposition: attachment`). **No se exporta el reporte de movimientos.**

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)

| Ruta                                          | Reporte de origen              | Parámetros (además de `formato`) | Nombre del archivo          |
|-----------------------------------------------|--------------------------------|----------------------------------|-----------------------------|
| `GET /api/reportes/valor-inventario/exportar` | `/api/reportes/valor-inventario` | —                                | `reporte-valor-inventario`  |

- **Parámetro `formato` (obligatorio):**

| Valor           | Content-Type                                                        | Extensión |
|-----------------|---------------------------------------------------------------------|-----------|
| `pdf`           | `application/pdf`                                                   | `.pdf`    |
| `excel`, `xlsx` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | `.xlsx`   |

El valor de `formato` no distingue mayúsculas. El archivo incluye el detalle por producto (código, nombre, stock
actual, costo y valor estimado) y un subtítulo con el total general y los conteos de productos con y sin costo.

- **Ejemplo:** `GET /api/reportes/valor-inventario/exportar?formato=pdf`
- **Respuesta 200 OK:** cuerpo binario del archivo.
- **Errores:** `400 VALIDATION_ERROR` (`formato` ausente o no válido), `401`, `403 FORBIDDEN`.

---

## 10. Dominio: Estadísticas

### 10.1 GET `/api/estadisticas`

Cuenta movimientos en un rango de fechas y los agrupa según la granularidad indicada.

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)
- **Parámetros de URL:**

| Parámetro     | Tipo                | Obligatorio | Descripción             |
|---------------|---------------------|-------------|-------------------------|
| `from`        | date (`yyyy-MM-dd`) | Sí*         | Fecha inicial inclusive |
| `to`          | date (`yyyy-MM-dd`) | Sí*         | Fecha final inclusive   |
| `granularity` | string              | Sí*         | `DAY`, `WEEK` o `MONTH` |

\* En el controlador figuran como opcionales, pero el servicio exige los tres y devuelve `400` si falta alguno.

- **Ejemplo:** `GET /api/estadisticas?from=2026-10-01&to=2026-10-31&granularity=DAY`
- **Respuesta 200 OK:** objeto cuyas claves son la fecha de inicio de cada período y los valores son conteos de
  movimientos. Los períodos sin movimientos se omiten. Con `DAY` la clave es el día; con `WEEK`, el lunes de la semana;
  con `MONTH`, el día 1 del mes. Los cortes se calculan en la zona horaria `America/Lima`.

```json
{
  "2026-10-01": 3,
  "2026-10-03": 5
}
```

- **Errores:** `400 VALIDATION_ERROR` (faltan parámetros, granularidad inválida, `to < from`), `401`, `403 FORBIDDEN`.

---

### 10.2 GET `/api/estadisticas/por-tipo`

Unidades y costos movilizados por período y tipo de movimiento. A diferencia de §10.1, aquí se cuentan unidades (no
movimientos) y se separan entradas, consumos y ajustes. Los costos se calculan sobre la tabla de movimientos como
`cantidad × costoUnitario` de cada movimiento, con el costo vigente registrado al momento de la operación.

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)
- **Parámetros de URL:**

| Parámetro     | Tipo                | Obligatorio | Descripción                          |
|---------------|---------------------|-------------|--------------------------------------|
| `from`        | date (`yyyy-MM-dd`) | Sí*         | Fecha inicial inclusive              |
| `to`          | date (`yyyy-MM-dd`) | Sí*         | Fecha final inclusive                |
| `granularity` | string              | Sí*         | `DAY`, `WEEK` o `MONTH`              |
| `productoId`  | integer             | No          | Filtra las unidades y costos de un producto |

\* En el controlador figuran como opcionales, pero el servicio exige los tres primeros y devuelve `400` si falta alguno.

- **Ejemplo:** `GET /api/estadisticas/por-tipo?from=2026-10-01&to=2026-10-31&granularity=WEEK&productoId=12`
- **Respuesta 200 OK:** array de `EstadisticasTipoPunto`. `fecha` es el inicio del período; `entrada` y `consumo` suman
  `cantidad`; `ajuste` suma el valor absoluto de `variacion`. Los períodos sin movimientos aparecen en cero.
  `costoEntradas` y `costoConsumos` son `Σ(cantidad × costoUnitario)` del período, redondeados a 2 decimales con
  `HALF_UP`. `costoAjustes` se devuelve siempre en `0`: los ajustes se registran con costo para trazabilidad, pero no
  se valoran en este reporte. Los movimientos sin costo aportan `0` al importe sin dejar de contar sus unidades.

```json
[
  {
    "fecha": "2026-10-06",
    "entrada": 40, "consumo": 12, "ajuste": 3,
    "costoEntradas": 170.00, "costoConsumos": 68.00, "costoAjustes": 0.00
  },
  {
    "fecha": "2026-10-13",
    "entrada": 0, "consumo": 0, "ajuste": 0,
    "costoEntradas": 0.00, "costoConsumos": 0.00, "costoAjustes": 0.00
  }
]
```

- **Errores:** `400 VALIDATION_ERROR` (faltan parámetros, granularidad inválida, `to < from`), `401`, `403 FORBIDDEN`.

---

## 11. Infraestructura

### 11.1 GET `/actuator/health`

- **Acceso:** Público
- **Respuesta 200 OK:** estado de salud de la aplicación (formato estándar de Spring Boot Actuator).

### 11.2 GET `/actuator/info`

- **Acceso:** Público
- **Respuesta 200 OK:** información de la aplicación, según configuración de Actuator.

---

## 12. Trazabilidad de reglas migradas

Este catálogo conserva las especificaciones de API que antes formaban parte del catálogo de reglas de negocio. La
tabla siguiente permite localizar su origen documental; no modifica los contratos descritos en las secciones
anteriores. Los identificadores `RN` marcados con un código original (`M100-`, `M200-`, `M400-`) se asignaron en la
reclasificación.

| Regla (ID original) | Contenido conservado en este catálogo | Sección |
|---------------------|----------------------------------------|---------|
| RN119 (M100-03) | Paginación, filtros, orden e inclusión de productos inactivos | §5.2 |
| RN210 (M200-01) | Semántica de `cantidad` y `variacion` por tipo de movimiento | §6.4 |
| RN211 (M200-02) | Desbordamiento al sumar cantidades repetidas en un consumo | §6.5 |
| RN212 (M200-03) | Listado operativo de existencias: solo activos, filtros y paginación | §6.1 |
| RN303 | Contenido mínimo de la alerta de bajo stock | §7.1 |
| RN405 | Parámetros y validación de rangos de fechas y granularidad | §9.1, §10.1 |
| RN409 (M400-01) | Cortes de fecha en `America/Lima` y respuestas en UTC | §9.1, §10.1 |
| RN410 (M400-02) | Omisión de períodos sin movimientos y claves de período | §10.1 |
| RN412 (M400-04) | Formatos de exportación y entrega como archivo adjunto | §9.3 |
| RN413 (M400-05) | Contenido del dashboard | §8.1 |
| RN414 (M400-07) | Paginación, orden y totales del reporte de movimientos | §9.1 |

Los detalles de validación se documentan en [VALIDACIONES.md](VALIDACIONES.md); las reglas de negocio en
[REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md); y los requisitos funcionales en
[REQUISITOS_FUNCIONALES.md](REQUISITOS_FUNCIONALES.md).

## 13. Unidades (V9/V10)

Las unidades y sus relaciones con las categorías son datos de negocio sembrados en las migraciones (unidades en V9;
categorías base y relaciones `categoria_unidad` en V10). La API solo las consulta: no hay endpoints para crearlas,
modificarlas, desactivarlas ni asignarlas a una categoría.

| Endpoint | Método | Acceso | Descripción |
|---|---|---|---|
| `/api/unidades` | GET | ADMIN, JEFE_TALLER, MECANICO | Catálogo de unidades activas (`id`, `codigo`, `nombre`, `permiteFragmentacion`) |
| `/api/categorias/{id}/unidades` | GET | JEFE_TALLER, MECANICO | Unidades válidas para esa categoría; alimenta el combobox del formulario de producto |

`ProductoResponse` incorpora `unidadBaseId`, `unidadBaseCodigo`, `unidadBaseNombre`, `unidadPresentacionId`,
`unidadPresentacionCodigo` y `factorPresentacion`. `CreateProductoRequest`/`UpdateProductoRequest` usan esos IDs;
la columna `unidad_medida` fue eliminada en V10.

`EntradaRequest`, `ConsumoItemRequest` y `AjusteRequest` aceptan `unidadRegistro`, `factorAplicado` y un campo
`cantidadExacta`/`cantidadFisicaExacta` (decimal, solo para longitud). `MovimientoResponse` y los reportes incluyen
`unidadRegistro`, `factorAplicado` y `cantidadBase`, todos como snapshot histórico.

Ejemplo de entrada de 3 cajas de 20 unidades a costo 25 por caja:

```json
{
  "productoId": 1,
  "proveedorId": 2,
  "cantidad": 3,
  "unidadRegistro": "CAJA",
  "factorAplicado": 20,
  "costoUnitario": 25.00
}
```

Respuesta: `cantidad = 60`, `cantidadBase = 60`, `variacion = 60`, `costoUnitario = 1.25`, `stockAnterior = 0`,
`stockPosterior = 60`.
