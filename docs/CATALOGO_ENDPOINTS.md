# Catálogo de endpoints — Sistema de Gestión de Inventario (Taller Mecánico)

Documento generado a partir del código actual: controladores, DTOs, `SecurityConfig`, anotaciones `@PreAuthorize` y
`GlobalExceptionHandler`.

Documentos relacionados: [REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md), [VALIDACIONES.md](VALIDACIONES.md),
[POLITICAS_DE_SEGURIDAD.md](POLITICAS_DE_SEGURIDAD.md), [CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md),
y [REQUISITOS_FUNCIONALES.md](REQUISITOS_FUNCIONALES.md).

- **Prefijo global:** `/api`
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
| Catálogo        | POST   | `/api/categorias`                    |  ❌   |     ✅      |    ❌    |    —    |
| Catálogo        | POST   | `/api/proveedores`                   |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | POST   | `/api/productos`                     |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | GET    | `/api/productos`                     |  ❌   |     ✅      |    ✅    |    —    |
| Productos       | PUT    | `/api/productos/{id}`                |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | PUT    | `/api/productos/{id}/stock-minimo`   |  ❌   |     ✅      |    ❌    |    —    |
| Productos       | PATCH  | `/api/productos/{id}/desactivar`     |  ❌   |     ✅      |    ❌    |    —    |
| Inventario      | GET    | `/api/existencias`                   |  ❌   |     ✅      |    ✅    |    —    |
| Inventario      | POST   | `/api/movimientos/entrada`           |  ❌   |     ✅      |    ❌    |    —    |
| Inventario      | POST   | `/api/movimientos/ajuste`            |  ❌   |     ✅      |    ❌    |    —    |
| Inventario      | POST   | `/api/consumos`                      |  ❌   |     ✅      |    ✅    |    —    |
| Alertas         | GET    | `/api/alertas`                       |  ❌   |     ✅      |    ✅    |    —    |
| Dashboard       | GET    | `/api/dashboard`                     |  ❌   |     ✅      |    ❌    |    —    |
| Reportes        | GET    | `/api/reportes/movimientos`          |  ❌   |     ✅      |    ❌    |    —    |
| Reportes        | GET    | `/api/reportes/consumo`              |  ❌   |     ✅      |    ❌    |    —    |
| Reportes        | GET    | `/api/reportes/valor-inventario`     |  ❌   |     ✅      |    ❌    |    —    |
| Reportes        | GET    | `/api/reportes/bajo-stock`           |  ❌   |     ✅      |    ❌    |    —    |
| Exportación     | GET    | `/api/reportes/movimientos/exportar` |  ❌   |     ✅      |    ❌    |    —    |
| Exportación     | GET    | `/api/reportes/consumo/exportar`     |  ❌   |     ✅      |    ❌    |    —    |
| Exportación     | GET    | `/api/reportes/bajo-stock/exportar`  |  ❌   |     ✅      |    ❌    |    —    |
| Estadísticas    | GET    | `/api/estadisticas`                  |  ❌   |     ✅      |    ❌    |    —    |
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

- **Errores:** `400 VALIDATION_ERROR`, `401 INVALID_CREDENTIALS`.

---

## 4. Dominio: Catálogo (categorías y proveedores)

### 4.1 POST `/api/categorias`

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

> No existen endpoints para listar, actualizar o desactivar categorías/proveedores.

---

## 5. Dominio: Productos

### 5.1 POST `/api/productos`

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

- **Respuesta 201 Created:** objeto `ProductoResponse` (ver sección 5.6).
- **Errores:** `400 VALIDATION_ERROR` (incluye categoría/proveedor inexistente o inactivo), `401`, `403 FORBIDDEN`,
  `409 DUPLICATE_RESOURCE` (el `codigo` ya existe).

---

### 5.2 GET `/api/productos`

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
- **Respuesta 200 OK:** `content` (lista de `ProductoResponse`) más cuatro campos de paginación.

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
| `codigo`       | string  | Sí          | No vacío, máximo 50, único            |
| `nombre`       | string  | Sí          | No vacío, máximo 150                  |
| `descripcion`  | string  | No          | Máximo 500                            |
| `unidadMedida` | string  | Sí          | No vacío, máximo 30                   |
| `stockMinimo`  | integer | Sí          | `>= 0`                                |
| `activo`       | boolean | No          | Si se omite, conserva el valor actual |
| `id_categoria` | integer | Sí          | `> 0`, existente y activa             |
| `id_proveedor` | integer | Sí          | `> 0`, existente y activo             |

- **Respuesta 200 OK:** `ProductoResponse`.
- **Errores:** `400 VALIDATION_ERROR`, `401`, `403 FORBIDDEN`, `404 NOT_FOUND`, `409 DUPLICATE_RESOURCE` (el `codigo`
  ya pertenece a otro producto).

> Si el producto ya tiene movimientos registrados, solo se permite cambiar `nombre`, `descripcion` y `unidadMedida`;
> `codigo`, `stockMinimo`, `activo`, categoría y proveedor deben enviarse sin cambios (si no, `400`).

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

### 5.6 Estructura `ProductoResponse`

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

## 6. Dominio: Inventario (existencias y movimientos)

### 6.1 GET `/api/existencias`

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

- **Errores:** `400 VALIDATION_ERROR` (sin `ids`, producto inactivo), `401`, `403 FORBIDDEN`, `404 NOT_FOUND` (algún ID
  no existe).

> Los identificadores repetidos en `ids` se ignoran: cada producto se consulta una sola vez (RN212).

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

## 7. Dominio: Alertas

### 7.1 GET `/api/alertas`

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

## 9. Dominio: Reportes

> Solo `GET /api/reportes/movimientos` está paginado. El resto de reportes (`consumo`, `valor-inventario`, `bajo-stock`) y
> todas las exportaciones devuelven el resultado completo.

### 9.1 GET `/api/reportes/movimientos`

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

- **Ejemplo:** `GET /api/reportes/movimientos?desde=2026-10-01&hasta=2026-10-31&tipo=ENTRADA&page=0&size=10`
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

### 9.3 GET `/api/reportes/bajo-stock`

Lista los productos con bajo stock agrupados por proveedor (orden alfabético).

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)
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

### 9.4 GET `/api/reportes/consumo`

Reporte de consumo en un rango de fechas. Toma los movimientos de tipo `CONSUMO` y los agrupa por producto y mecánico,
ordenados por nombre de producto y luego por mecánico.

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)
- **Parámetros de URL:**

| Parámetro | Tipo                | Obligatorio | Descripción             |
|-----------|---------------------|-------------|-------------------------|
| `desde`   | date (`yyyy-MM-dd`) | Sí*         | Fecha inicial inclusive |
| `hasta`   | date (`yyyy-MM-dd`) | Sí*         | Fecha final inclusive   |

\* En el controlador figuran como opcionales, pero el servicio exige ambos y devuelve `400` si falta alguno.

- **Ejemplo:** `GET /api/reportes/consumo?desde=2026-10-01&hasta=2026-10-31`
- **Respuesta 200 OK:**

```json
[
  {
    "productoId": 12,
    "productoCodigo": "FIL-001",
    "productoNombre": "Filtro de aceite",
    "mecanico": "mecanico1",
    "finalidades": [
      "Cambio de aceite"
    ],
    "cantidadTotal": 6,
    "fechas": [
      "2026-10-05T14:10:00Z",
      "2026-10-12T16:30:00Z"
    ]
  }
]
```

- `finalidades` lista las finalidades distintas declaradas; `fechas` contiene el instante de cada movimiento agrupado.
- **Errores:** `400 VALIDATION_ERROR` (faltan fechas, `hasta < desde`, formato inválido), `401`, `403 FORBIDDEN`.

---

### 9.5 Exportación de reportes (PDF y Excel)

Devuelven el mismo contenido del reporte equivalente como archivo adjunto (`Content-Disposition: attachment`).

- **Acceso:** `JEFE_TALLER` (**ADMIN y MECANICO reciben 403**)
- **Endpoints:**

| Ruta                                     | Reporte de origen           | Parámetros (además de `formato`) | Nombre del archivo    |
|------------------------------------------|-----------------------------|----------------------------------|-----------------------|
| `GET /api/reportes/movimientos/exportar` | `/api/reportes/movimientos` | `desde`, `hasta`, `tipo`         | `reporte-movimientos` |
| `GET /api/reportes/consumo/exportar`     | `/api/reportes/consumo`     | `desde`, `hasta`                 | `reporte-consumo`     |
| `GET /api/reportes/bajo-stock/exportar`  | `/api/reportes/bajo-stock`  | —                                | `reporte-bajo-stock`  |

- **Parámetro `formato` (obligatorio):**

| Valor           | Content-Type                                                        | Extensión |
|-----------------|---------------------------------------------------------------------|-----------|
| `pdf`           | `application/pdf`                                                   | `.pdf`    |
| `excel`, `xlsx` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | `.xlsx`   |

El valor de `formato` no distingue mayúsculas. Las reglas de `desde`, `hasta` y `tipo` son las del reporte de origen.
Las exportaciones no se paginan: ignoran `page` y `size` y exportan el reporte completo.

- **Ejemplo:** `GET /api/reportes/movimientos/exportar?formato=pdf&desde=2026-10-01&hasta=2026-10-31&tipo=ENTRADA`
- **Respuesta 200 OK:** cuerpo binario del archivo.
- **Errores:** `400 VALIDATION_ERROR` (`formato` ausente o no válido, fechas inválidas), `401`, `403 FORBIDDEN`.

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
| RN212 (M200-03) | Consulta de existencias por `ids`, duplicados, inexistentes e inactivos | §6.1 |
| RN303 | Contenido mínimo de la alerta de bajo stock | §7.1 |
| RN404 | Contenido, agrupación y orden del reporte de consumo | §9.4 |
| RN405 | Parámetros y validación de rangos de fechas y granularidad | §9.1, §9.4, §10.1 |
| RN409 (M400-01) | Cortes de fecha en `America/Lima` y respuestas en UTC | §9.1, §9.4, §10.1 |
| RN410 (M400-02) | Omisión de períodos sin movimientos y claves de período | §10.1 |
| RN411 (M400-03) | Agrupación por proveedor en orden alfabético | §9.3 |
| RN412 (M400-04) | Formatos de exportación y entrega como archivo adjunto | §9.5 |
| RN413 (M400-05) | Contenido del dashboard | §8.1 |
| RN414 (M400-07) | Paginación, orden y totales del reporte de movimientos | §9.1 |

Los detalles de validación se documentan en [VALIDACIONES.md](VALIDACIONES.md); las reglas de negocio en
[REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md); y los requisitos funcionales en
[REQUISITOS_FUNCIONALES.md](REQUISITOS_FUNCIONALES.md).
