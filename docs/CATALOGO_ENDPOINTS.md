# Catálogo de endpoints — Sistema de Gestión de Inventario (Taller Mecánico)

Documento generado a partir del código actual: controladores, DTOs, `SecurityConfig`, anotaciones `@PreAuthorize` y
`GlobalExceptionHandler`.

- **Prefijo global:** `/api`
- **Base URL:** `http://<host>:${SERVER_PORT}`
- **Formato:** `application/json`
- **Autenticación:** JWT en cabecera `Authorization: Bearer <token>`
- **Fechas:** `Instant` en UTC con formato `yyyy-MM-dd'T'HH:mm:ss'Z'`; `LocalDate` con formato `yyyy-MM-dd`

## 1. Roles

| Rol           | Descripción                                                                                                |
|---------------|------------------------------------------------------------------------------------------------------------|
| `ADMIN`       | Usuario semilla. Único rol que puede registrar usuarios. No puede asignarse mediante `/api/auth/register`. |
| `JEFE_TALLER` | Administra catálogo, productos, entradas y ajustes de inventario.                                          |
| `MECANICO`    | Consulta productos, existencias y alertas. No accede a dashboard, reportes ni estadísticas. No modifica catálogo ni stock. |

## 2. Contrato de error

Todas las excepciones controladas por `GlobalExceptionHandler` usan este formato:

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

`details` se omite cuando está vacío (`@JsonInclude(NON_EMPTY)`).

| HTTP | Código `error`             | Causa                                                                                                          |
|------|----------------------------|----------------------------------------------------------------------------------------------------------------|
| 400  | `VALIDATION_ERROR`         | Bean Validation, JSON inválido, parámetro con formato inválido, regla de negocio (`IllegalArgumentException`). |
| 400  | `INVALID_ROLE`             | Rol inexistente o intento de registrar `ADMIN`.                                                                |
| 401  | `INVALID_CREDENTIALS`      | Usuario o contraseña incorrectos en login.                                                                     |
| 401  | `UNAUTHORIZED`             | Falta el token.                                                                                                |
| 401  | `TOKEN_EXPIRED`            | Token expirado.                                                                                                |
| 401  | `TOKEN_INVALID`            | Token malformado o usuario inexistente.                                                                        |
| 401  | `ACCOUNT_DISABLED`         | Usuario inactivo.                                                                                              |
| 403  | `FORBIDDEN`                | Rol sin permisos para la operación.                                                                            |
| 404  | `NOT_FOUND`                | Producto o recurso inexistente.                                                                                |
| 409  | `USER_ALREADY_EXISTS`      | Username ya registrado.                                                                                        |
| 409  | `DUPLICATE_RESOURCE`       | Categoría duplicada (nombre) o proveedor duplicado (RUC).                                                      |
| 409  | `DATA_INTEGRITY_VIOLATION` | Violación de restricción de base de datos.                                                                     |

> Las excepciones no controladas devuelven el formato de error por defecto de Spring (HTTP 500), no este contrato.

## 3. Resumen de endpoints y matriz de acceso

| Dominio         | Método | Ruta                               | ADMIN | JEFE_TALLER | MECANICO | Público |
|-----------------|--------|------------------------------------|:-----:|:-----------:|:--------:|:-------:|
| Seguridad       | POST   | `/api/auth/register`               |  ✅   |     ❌      |    ❌    |    —    |
| Seguridad       | POST   | `/api/auth/login`                  |   —   |      —      |    —     |   ✅    |
| Catálogo        | POST   | `/api/categorias`                  |  ❌   |     ✅      |    ❌    |    —    |
| Catálogo        | POST   | `/api/proveedores`                 |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | POST   | `/api/productos`                   |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | GET    | `/api/productos`                   |  ❌   |     ✅      |    ✅    |    —    |
| Productos       | PUT    | `/api/productos/{id}`              |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | PUT    | `/api/productos/{id}/stock-minimo` |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | PATCH  | `/api/productos/{id}/desactivar`   |  ❌   |     ✅      |    ❌    |    —    |
| Inventario      | GET    | `/api/existencias`                 |  ❌   |     ✅      |    ✅    |    —    |
| Inventario      | POST   | `/api/movimientos/entrada`         |  ❌   |     ✅      |    ❌    |    —    |
| Inventario      | POST   | `/api/movimientos/ajuste`          |  ❌   |     ✅      |    ❌    |    —    |
| Alertas         | GET    | `/api/alertas`                     |  ❌   |     ✅      |    ✅    |    —    |
| Dashboard       | GET    | `/api/dashboard`                   |  ❌   |     ✅      |    ❌    |    —    |
| Reportes        | GET    | `/api/reportes/movimientos`        |  ❌   |     ✅      |    ❌    |    —    |
| Reportes        | GET    | `/api/reportes/valor-inventario`   |  ❌   |     ✅      |    ❌    |    —    |
| Reportes        | GET    | `/api/reportes/bajo-stock`         |  ❌   |     ✅      |    ❌    |    —    |
| Estadísticas    | GET    | `/api/estadisticas`                |  ❌   |     ✅      |    ❌    |    —    |
| Infraestructura | GET    | `/actuator/health`                 |   —   |      —      |    —     |   ✅    |
| Infraestructura | GET    | `/actuator/info`                   |  ✅   |     ❌      |    ❌    |    —    |

---

## 4. Dominio: Seguridad

### 4.1 POST `/api/auth/register`

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

### 4.2 POST `/api/auth/login`

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

- **Errores:** `400 VALIDATION_ERROR`, `401 INVALID_CREDENTIALS`.

---

## 5. Dominio: Catálogo (categorías y proveedores)

### 5.1 POST `/api/categorias`

Crea una categoría. El nombre es obligatorio y único sin distinguir mayúsculas/minúsculas.

- **Acceso:** `JEFE_TALLER`
- **Body:**

| Campo         | Tipo   | Obligatorio | Validación                      |
|---------------|--------|-------------|---------------------------------|
| `nombre`      | string | Sí          | No vacío, máximo 100 caracteres |
| `descripcion` | string | No          | Máximo 255 caracteres           |

```json
{
  "nombre": "Filtros",
  "descripcion": "Filtros de aceite y aire"
}
```

- **Respuesta 201 Created:**

```json
{
  "id": 3,
  "nombre": "Filtros",
  "descripcion": "Filtros de aceite y aire",
  "activo": true
}
```

- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `409 DUPLICATE_RESOURCE`.

---

### 5.2 POST `/api/proveedores`

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

> No existen endpoints para listar, actualizar o desactivar categorías/proveedores.

---

## 6. Dominio: Productos

### 6.1 POST `/api/productos`

Crea un producto. El stock inicial siempre es `0`; el SKU se genera automáticamente con el patrón `CP-000000`.

- **Acceso:** `JEFE_TALLER`
- **Body:**

| Campo          | Tipo    | Obligatorio | Validación                                      |
|----------------|---------|-------------|-------------------------------------------------|
| `codigo`       | string  | Sí          | No vacío, máximo 50 caracteres, único           |
| `nombre`       | string  | Sí          | No vacío, máximo 150 caracteres                 |
| `descripcion`  | string  | No          | Máximo 500 caracteres                           |
| `unidadMedida` | string  | Sí          | No vacío, máximo 30 caracteres                  |
| `stockMinimo`  | integer | Sí          | `>= 0`                                          |
| `id_categoria` | integer | Sí          | `> 0`; la categoría debe existir y estar activa |
| `id_proveedor` | integer | Sí          | `> 0`; el proveedor debe existir y estar activo |

Los campos `id_categoria` e `id_proveedor` aceptan como alias `idCategoria`/`categoriaId` y `idProveedor`/`proveedorId`.

```json
{
  "codigo": "FIL-001",
  "nombre": "Filtro de aceite",
  "descripcion": "Filtro para motor 1.6L",
  "unidadMedida": "UNIDAD",
  "stockMinimo": 10,
  "id_categoria": 3,
  "id_proveedor": 7
}
```

- **Respuesta 201 Created:** objeto `ProductoResponse` (ver sección 6.6).
- **Errores:** `400 VALIDATION_ERROR` (incluye código duplicado, categoría/proveedor inexistente o inactivo), `401`,
  `403 FORBIDDEN`.

---

### 6.2 GET `/api/productos`

Lista productos con paginación y filtros opcionales.

- **Acceso:** `JEFE_TALLER`, `MECANICO`
- **Parámetros de URL:**

| Parámetro   | Tipo    | Obligatorio | Default | Descripción                                            |
|-------------|---------|-------------|---------|--------------------------------------------------------|
| `page`      | integer | No          | `0`     | Número de página (se normaliza a `>= 0`)               |
| `size`      | integer | No          | `10`    | Tamaño de página (se limita a 1–100)                   |
| `nombre`    | string  | No          | —       | Búsqueda parcial por nombre, sin distinguir mayúsculas |
| `categoria` | integer | No          | —       | Filtra por ID de categoría                             |

- **Ejemplo:** `GET /api/productos?page=0&size=10&nombre=filtro&categoria=3`
- **Respuesta 200 OK:** página de Spring con `content` de `ProductoResponse`.

```json
{
  "content": [
    {
      "id": 12,
      "codigo": "FIL-001",
      "sku": "CP-000012",
      "nombre": "Filtro de aceite",
      "descripcion": "Filtro para motor 1.6L",
      "unidadMedida": "UNIDAD",
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
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

Orden por defecto: `nombre` ascendente y luego `id`.

- **Errores:** `400 VALIDATION_ERROR` (parámetro con formato inválido), `401`, `403 FORBIDDEN`.

---

### 6.3 PUT `/api/productos/{id}`

Actualiza un producto completo.

- **Acceso:** `JEFE_TALLER`
- **Path:** `id` (integer, obligatorio)
- **Body:**

| Campo          | Tipo    | Obligatorio | Validación                            |
|----------------|---------|-------------|---------------------------------------|
| `codigo`       | string  | Sí          | No vacío, máximo 50, único            |
| `nombre`       | string  | Sí          | No vacío, máximo 150                  |
| `descripcion`  | string  | No          | Máximo 500                            |
| `unidadMedida` | string  | Sí          | No vacío, máximo 30                   |
| `stockMinimo`  | integer | Sí          | `>= 0`                                |
| `activo`       | boolean | No          | Si se omite, conserva el valor actual |
| `id_categoria` | integer | Sí          | `> 0`, existente y activa             |
| `id_proveedor` | integer | Sí          | `> 0`, existente y activo             |

- **Respuesta 200 OK:** `ProductoResponse`.
- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `404 NOT_FOUND`.

> Si el producto ya tiene movimientos registrados, solo se permite cambiar `nombre`, `descripcion` y `unidadMedida`;
> `codigo`, `stockMinimo`, `activo`, categoría y proveedor deben enviarse sin cambios (si no, `400`).

---

### 6.4 PUT `/api/productos/{id}/stock-minimo`

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

### 6.5 PATCH `/api/productos/{id}/desactivar`

Desactiva lógicamente un producto (`activo = false`).

- **Acceso:** `JEFE_TALLER`
- **Path:** `id` (integer, obligatorio)
- **Body:** ninguno.
- **Respuesta 200 OK:** `ProductoResponse` con `activo: false`.
- **Errores:** `400 VALIDATION_ERROR` (producto con movimientos), `401`, `403 FORBIDDEN`, `404 NOT_FOUND`.

---

### 6.6 Estructura `ProductoResponse`

| Campo                  | Tipo    | Descripción                             |
|------------------------|---------|-----------------------------------------|
| `id`                   | integer | Identificador                           |
| `codigo`               | string  | Código único                            |
| `sku`                  | string  | SKU generado (`CP-` + 6 dígitos)        |
| `nombre`               | string  | Nombre                                  |
| `descripcion`          | string  | Descripción                             |
| `unidadMedida`         | string  | Unidad de medida                        |
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

## 7. Dominio: Inventario (existencias y movimientos)

### 7.1 GET `/api/existencias`

Consulta el stock de uno o varios productos activos.

- **Acceso:** `JEFE_TALLER`, `MECANICO`
- **Parámetros de URL:**

| Parámetro | Tipo             | Obligatorio | Descripción                                         |
|-----------|------------------|-------------|-----------------------------------------------------|
| `ids`     | lista de integer | Sí          | IDs de producto. Acepta `?ids=1&ids=2` o `?ids=1,2` |

- **Ejemplo:** `GET /api/existencias?ids=12&ids=15`
- **Respuesta 200 OK:**

```json
[
  {
    "productoId": 12,
    "codigo": "FIL-001",
    "nombre": "Filtro de aceite",
    "stockActual": 25,
    "stockMinimo": 10,
    "stockCritico": false
  }
]
```

- **Errores:** `400 VALIDATION_ERROR` (sin `ids`, producto inactivo), `401`, `403 FORBIDDEN`, `404 NOT_FOUND` (algún ID no existe).

---

### 7.2 POST `/api/movimientos/entrada`

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

### 7.3 POST `/api/movimientos/ajuste`

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

### 7.4 Estructura `MovimientoResponse`

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

> No existe endpoint para registrar consumos (`CONSUMO`), aunque el tipo está contemplado en el modelo y en reportes.

---

## 8. Dominio: Alertas

### 8.1 GET `/api/alertas`

Lista los productos activos con bajo stock (`stockActual <= stockMinimo`), ordenados por cantidad sugerida descendente y
luego por nombre.

- **Acceso:** `JEFE_TALLER`, `MECANICO`
- **Parámetros:** ninguno.
- **Respuesta 200 OK:**

```json
[
  {
    "codigo": "FIL-001",
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

## 9. Dominio: Dashboard

### 9.1 GET `/api/dashboard`

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
        "codigo": "FIL-001",
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

## 10. Dominio: Reportes

### 10.1 GET `/api/reportes/movimientos`

Reporte de movimientos en un rango de fechas, con totales por tipo.

- **Acceso:** `JEFE_TALLER`
- **Parámetros de URL:**

| Parámetro | Tipo                | Obligatorio | Descripción                     |
|-----------|---------------------|-------------|---------------------------------|
| `desde`   | date (`yyyy-MM-dd`) | Sí*         | Fecha inicial inclusive         |
| `hasta`   | date (`yyyy-MM-dd`) | Sí*         | Fecha final inclusive           |
| `tipo`    | string              | No          | `ENTRADA`, `CONSUMO` o `AJUSTE` |

\* En el controlador figuran como opcionales, pero el servicio exige ambos y devuelve `400` si falta alguno.

- **Ejemplo:** `GET /api/reportes/movimientos?desde=2026-10-01&hasta=2026-10-31&tipo=ENTRADA`
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
      "consumoId": null,
      "usuarioId": 2
    }
  ]
}
```

- **Errores:** `400 VALIDATION_ERROR` (faltan fechas, `hasta < desde`, formato inválido), `401`, `403 FORBIDDEN`.

---

### 10.2 GET `/api/reportes/valor-inventario`

Calcula el valor estimado del inventario activo: `stockActual * costoUltimaCompra` por producto.

- **Acceso:** `JEFE_TALLER`
- **Parámetros:** ninguno.
- **Respuesta 200 OK:**

```json
{
  "totalGeneral": 12500.75,
  "totalProductosConCosto": 37,
  "productosSinCosto": 3,
  "detalle": [
    {
      "codigo": "FIL-001",
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
  ]
}
```

Los productos sin costo se incluyen en `detalle` con `costoUltimaCompra: null` y valor `0.00`, y no suman al total.

- **Errores:** `401`, `403 FORBIDDEN`.

---

### 10.3 GET `/api/reportes/bajo-stock`

Lista los productos con bajo stock agrupados por proveedor (orden alfabético).

- **Acceso:** `JEFE_TALLER`
- **Parámetros:** ninguno.
- **Respuesta 200 OK:**

```json
[
  {
    "proveedor": "Repuestos Andinos S.A.",
    "productos": [
      {
        "codigo": "FIL-001",
        "nombre": "Filtro de aceite",
        "stockActual": 4,
        "stockMinimo": 10,
        "sugerido": 6
      }
    ]
  }
]
```

- **Errores:** `401`, `403 FORBIDDEN`.

---

## 11. Dominio: Estadísticas

### 11.1 GET `/api/estadisticas`

Cuenta movimientos en un rango de fechas y los agrupa según la granularidad indicada.

- **Acceso:** `JEFE_TALLER`
- **Parámetros de URL:**

| Parámetro     | Tipo                | Obligatorio | Descripción             |
|---------------|---------------------|-------------|-------------------------|
| `from`        | date (`yyyy-MM-dd`) | Sí*         | Fecha inicial inclusive |
| `to`          | date (`yyyy-MM-dd`) | Sí*         | Fecha final inclusive   |
| `granularity` | string              | Sí*         | `DAY`, `WEEK` o `MONTH` |

\* En el controlador figuran como opcionales, pero el servicio exige los tres y devuelve `400` si falta alguno.

- **Ejemplo:** `GET /api/estadisticas?from=2026-10-01&to=2026-10-31&granularity=DAY`
- **Respuesta 200 OK:** objeto cuyas claves son fechas y los valores son conteos.

```json
{
  "2026-10-01": 3,
  "2026-10-02": 0,
  "2026-10-03": 5
}
```

- **Errores:** `400 VALIDATION_ERROR` (faltan parámetros, granularidad inválida, `to < from`), `401`, `403 FORBIDDEN`.

---

## 12. Infraestructura

### 12.1 GET `/actuator/health`

- **Acceso:** Público
- **Respuesta 200 OK:** estado de salud de la aplicación (formato estándar de Spring Boot Actuator).

### 12.2 GET `/actuator/info`

- **Acceso:** `ADMIN` (expuesto por configuración de Actuator)
- **Respuesta 200 OK:** información de la aplicación, según configuración de Actuator.

