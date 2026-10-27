# **Sistema de Gestión de Inventario — Taller Mecánico**

## **Información general**

El Sistema de Gestión de Inventario del Taller Mecánico es un backend construido como un **monolito modular**: una sola aplicación Spring Boot, dividida en módulos internos, que se conecta a una única base de datos PostgreSQL. Todas sus configuraciones se obtienen exclusivamente de variables de entorno, sin valores por defecto.

| **Campo** | **Detalle** |
|---|---|
| Nombre del proyecto | Sistema de Gestión de Inventario — Taller Mecánico |
| Tipo | Backend con arquitectura de monolito modular |
| Paquete base | `com.taller` |
| Prefijo de la API | `/api` |
| Versionado de la API | API Versioning nativo de Spring Framework 7, incorporado en Spring Boot 4, por cabecera HTTP (header-based): la versión no se escribe en las rutas (sin `v1` ni `v2`) y el cliente la envía en la cabecera `X-API-Version` |
| Base de datos | PostgreSQL 15, base `inventario_taller` |
| Configuración | Solo variables de entorno, sin valores por defecto; validación Fail-Fast al arrancar |
| Puerto | Variable `SERVER_PORT` (8080 en el `.env` de ejemplo) |
| Equipo | 4 integrantes |

## **Descripción del sistema**

El sistema cubre el ciclo de inventario de un taller mecánico: catálogo de productos, categorías y proveedores, registro de entradas, consumos y ajustes de stock, configuración de stock mínimo, alertas de bajo stock, reportes, dashboard y estadísticas de movimientos.

Todos los módulos se ejecutan en el mismo proceso y comparten la misma base de datos. La seguridad (JWT y roles) se aplica con un único filtro que intercepta todas las peticiones.

### **Módulos del sistema**

| **Módulo** | **Código** | **Responsabilidad** | **Historias** | **Consume a** |
|---|---|---|---|---|
| Seguridad | SEC | Registro, login, emisión y validación de JWT, roles y permisos | US-01 a US-03 | — |
| Gestión de Productos | M100 | Catálogo de productos, categorías y proveedores | US-04, US-05 | — |
| Gestión de Inventario | M200 | Entradas, consumos, ajustes, validación de stock y consulta de existencias | US-06, US-07, US-08, US-11, US-12, US-13 | M100 |
| Control y Abastecimiento | M300 | Stock mínimo y alertas de bajo stock | US-14, US-15, US-16 | M100 |
| Consultas y Reportes | M400 | Reportes, exportación, dashboard y estadísticas | US-17 a US-23 | M100, M200, M300, SEC |
| Transversal | — | README, Swagger y criterio de pruebas unitarias | US-26 | Todos |

Todos los módulos dependen de SEC para autenticar y autorizar las peticiones (`JwtAuthenticationFilter`).

## **Tecnologías y herramientas**

| **Categoría** | **Tecnología** |
|---|---|
| Lenguaje | Java 21 (Pattern Matching, Records, Foreign Key API, Grains) |
| Framework | Spring Boot 4 |
| Versionado de API | API Versioning nativo de Spring Framework 7, incorporado en Spring Boot 4 (versionado por cabecera HTTP) |
| Seguridad | Spring Security 6 + JWT (HS256) con validación de variables de entorno obligatorias (Fail-Fast) |
| Base de datos | PostgreSQL 15 |
| Migraciones | Flyway |
| Acceso a datos | Spring Data JPA (`JpaRepository`), HikariCP |
| Build | Maven |
| Configuración | `application.yml` |
| Exportación | `itextpdf` o Apache POI (`poi`) |
| Documentación | Swagger UI / OpenAPI 3.0 automático |
| Testing | JUnit 5 + Mockito (pruebas unitarias) |
| Contenerización | Docker (opcional, `docker compose up` si se usa Docker) |

## **Estándares del proyecto**

Con el objetivo de garantizar consistencia, legibilidad y mantenibilidad del código, el proyecto debe seguir los siguientes estándares transversales.

### **Nomenclatura y formato de nombres**

- **Variables de entorno:** UPPER_SNAKE_CASE (ej: `DATASOURCE_URL`, `JWT_SECRET`, `INTERNAL_API_KEY`, `SERVER_PORT`). Deben ser descriptivas, sin espacios y compuestas únicamente por letras, números y guiones bajos.
- **Código Java:** clases en PascalCase, métodos y variables en camelCase, constantes en MAYUSCULAS. Todo el código y los identificadores deben estar en **inglés**.
- **Base de datos:** tablas y columnas en snake_case en minúsculas (ej: `movimiento_inventario`, `stock_actual`).

### **Formato de endpoints**

- Prefijo global: `/api`. La versión no se escribe en las rutas.
- Recursos en plural con sustantivos en minúsculas (ej: `/api/productos`, `/api/movimientos`).
- Sub-recursos con identificador (ej: `/api/productos/{id}`, `/api/productos/{id}/stock-minimo`).
- Uso correcto de verbos HTTP: POST (crear), GET (consultar), PUT (actualizar), PATCH (actualizar parcial), DELETE (eliminar).
- Versionado: se gestiona con el API Versioning nativo de Spring Framework 7, incorporado en Spring Boot 4, mediante versionado por cabecera HTTP (header-based), para versionar sin agregar `v1` o `v2` a la URL. Cada endpoint declara su versión con el atributo `version` de su anotación de mapeo (ej: `version = "1.0"`) y el cliente la envía en la cabecera `X-API-Version`. No se usa `/v1` en las rutas.

### **Manejo de variables de entorno**

- Todas las configuraciones deben obtenerse exclusivamente de variables de entorno.
- Formato en `application.yml`: `${NOMBRE_VARIABLE}`, sin valores por defecto.
- Validación Fail-Fast ante variables críticas faltantes (ver sección de configuración).
- Archivo `.env` en `.gitignore`; en producción las variables se definen en el entorno de despliegue.

### **Restricciones**

- No se permiten valores por defecto en el código ni en la configuración.
- No se deben exponer secretos en el código fuente ni en logs.
- Toda entrada de usuario debe validarse en backend antes de procesarse.

### **Buenas prácticas**

- Validaciones consistentes en servicios y controladores.
- Manejo centralizado de errores con contrato estándar (ver sección de contrato de errores).
- Logging estructurado y sin información sensible.
- Documentación de endpoints en Swagger/OpenAPI.

## **Reglas de negocio**

Listado consolidado de las reglas de negocio (RN) del sistema, con el módulo al que pertenece cada una y las historias de usuario que la tocan. El detalle de cada regla está también en la sección de su módulo. La numeración es la del documento original.

| **Regla** | **Descripción** | **Módulo** | **Historias** |
|---|---|---|---|
| RN101 | El campo `codigo` debe ser único. Validar antes de guardar | M100 | US-04 |
| RN102 | `nombre` y `categoria` son obligatorios; no se guarda si están vacíos. La categoría debe existir y estar activa para asociarse a un producto | M100 | US-04, US-05 |
| RN103 | `unidadMedida` es obligatoria y principal | M100 | US-04 |
| RN104 | `stockMinimo` numérico >= 0 | M100 (también M300) | US-04, US-08, US-14 |
| RN105 | Cada producto debe tener un `proveedorPrincipal` activo | M100 | US-04, US-05 |
| RN106 | El stock inicial de un producto nuevo es **0**. No se permite editar el stock desde el formulario de producto (solo por movimientos) | M100 | US-04 |
| RN107 | Solo el Jefe de taller podrá administrar productos, categorías y proveedores; el mecánico no podrá modificar estos catálogos. Si el token tiene `role=MECANICO`, los endpoints `/api/productos/guardar`, `/api/categorias/guardar` y `/api/proveedores/guardar` deben devolver `403 FORBIDDEN` | SEC | US-02, US-03 |
| RN108 | Los productos con movimientos históricos (registros en `movimiento_inventario`) **no** se pueden eliminar físicamente. Solo desactivación lógica (`activo = false`) | M100 | US-04 |
| RN109 | El RUC del proveedor (`ruc` o código de identificación) debe ser único | M100 | US-02, US-05 |
| RN201 | El Jefe de taller (rol `JEFE_TALLER`) registra la entrada con producto, proveedor, cantidad y costo unitario. El sistema asigna fecha y usuario automáticamente | M200 | US-06 |
| RN202 | Cantidad > 0. Costo unitario >= 0. El proveedor debe estar activo | M200 | US-06 |
| RN203 | Al confirmar, el sistema incrementa `stockActual` del producto y actualiza `costoUltimaCompra` con el costo unitario de esa entrada | M200 | US-06 |
| RN204 | El mecánico registra los productos utilizados directamente. No crea solicitud al Jefe | M200 | US-11 |
| RN205 | Cada consumo identifica al mecánico autenticado, fecha, productos, cantidades y finalidad. La referencia a vehículo/cliente es opcional | M200 | US-11 |
| RN206 | Cada producto consumido debe existir, estar activo y tener stock suficiente. Toda cantidad > 0 y en su unidad de medida. **No stock negativo:** cualquier operación que descuente stock debe verificar que el resultado no sea < 0 | M200 | US-07, US-11 |
| RN207 | Consumo atómico: si falla algún producto, **no** se descuenta nada parcial | M200 | US-11, US-12 |
| RN208 | Cada movimiento confirmado conserva tipo, producto, cantidad/variación, stock anterior y posterior, fecha y usuario, más motivo/justificación | M200 | US-13 |
| RN209 | Solo el Jefe de taller puede ajustar. Cantidad física >= 0. Justificación obligatoria. Los ajustes no modifican el costo de última compra | M200 | US-13 |
| RN301 | Cada producto activo tendrá un stock mínimo configurado | M300 | US-14 |
| RN302 | Se considerará bajo stock cuando `stock_actual <= stock_minimo` | M300 | US-08, US-15 |
| RN303 | La alerta mostrará como mínimo código, nombre, stock actual, stock mínimo y proveedor principal | M300 | US-15 |
| RN304 | Las alertas se recalculan después de cada movimiento (cambiar el mínimo afecta la alerta inmediata) | M300 | US-14, US-15 |
| RN305 | La cantidad sugerida es la diferencia `stock_minimo - stock_actual` (solo referencial) | M300 | US-15 |
| RN404 | El reporte de consumo permite identificar producto, cantidad utilizada, fecha, mecánico y finalidad declarada | M400 | US-18 |
| RN405 | Los filtros por fecha validarán que el rango sea correcto y que la fecha inicial no sea posterior a la final | M400 | US-17 |
| RN407 | El valor estimado es la suma de `stock_actual * costo_ultima_compra`. Es una estimación operativa, no contable oficial | M400 | US-20 |
| RN408 | Los reportes usarán datos actuales y movimientos confirmados, serán de solo lectura y conservarán la consulta histórica aunque un producto esté inactivo. Si un producto no tiene costo de última compra registrado, se identifica y se excluye del cálculo total, informando la cantidad de productos excluidos | M400 | US-17, US-20 |

Además, el módulo SEC describe dos reglas sin número de RN: la validación del token y la interceptación de peticiones por el filtro.

## **Módulo Seguridad — SEC**

### **Descripción**

Gestiona el registro y la autenticación de usuarios. Emite tokens JWT (HS256, validez de 3600 segundos según `JWT_EXPIRACION_SEGUNDOS`) con los claims `sub` (id del usuario), `username` y `role`. El filtro `JwtAuthenticationFilter` se ejecuta en cada petición, verifica la firma con `jwt.secret` y comprueba los roles antes de ejecutar los endpoints.

### **Roles**

| **Rol** | **Permisos** |
|---|---|
| JEFE_TALLER | Administra productos, categorías y proveedores (RN107); registra entradas (RN201) y ajustes (RN209); configura el stock mínimo |
| MECANICO | Registra consumos (RN204); no puede modificar los catálogos (RN107). Es el rol que se asigna al registrarse si no se especifica otro |
| ADMIN | Rol del usuario administrador inicial, que se inserta como usuario seed (no se crea mediante el registro) |

### **Reglas de negocio**

| **Regla** | **Descripción** |
|---|---|
| RN107 | Solo el Jefe de taller podrá administrar productos, categorías y proveedores; el mecánico no podrá modificar estos catálogos. Si el token tiene `role=MECANICO`, los endpoints `/api/productos/guardar`, `/api/categorias/guardar` y `/api/proveedores/guardar` deben devolver `403 FORBIDDEN` |
| RN109 | El RUC del proveedor debe ser único (aplicable en el registro de proveedores; la lógica de unicidad es similar a la del registro de usuarios) |
| Validación de token | El filtro verifica la firma HS256 usando `jwt.secret`. Si el token es inválido o está caducado, bloquea la petición y devuelve `401 UNAUTHORIZED` |
| Interceptación | El filtro intercepta las peticiones a `/api/vehiculos/**`, `/api/movimientos/**`, etc. Si el usuario es `MECANICO` y hace `POST /api/vehiculos`, el controlador debe verificar el rol y rechazar |

### **Endpoints requeridos**

#### **1. POST /api/auth/register**

**Acceso:** Público

**Descripción:** Registra un nuevo usuario. El rol es opcional: si se omite, se asigna `MECANICO`. El rol `ADMIN` no se asigna por registro: el usuario administrador se inserta como usuario seed.

#### **Body:**

```json
{ "username": "carlos", "email": "carlos@test.com", "password": "Pass123!", "role": "MECANICO" }
```

`role` es opcional: si se omite, se asigna el rol por defecto `MECANICO`.

#### **Response 201 Created:**

```json
{ "id": 1, "username": "carlos", "email": "carlos@test.com", "role": "MECANICO", "createdAt": "2026-10-05T10:30:00Z" }
```

#### **Validaciones:**

1. **Email/Username único:** consultar el repositorio. Si existe, lanzar `RuntimeException("Email already in use")`.
2. **Password Strength:** mínimo 8 caracteres, al menos 1 mayúscula y 1 número (validación personalizada en el servicio).
3. **Rol asignación:** el registro crea automáticamente un usuario con rol `MECANICO` si no se especifica otro. El rol `ADMIN` no se puede asignar por registro, porque el usuario administrador se inserta como usuario seed. La regla RN107 restringe permisos después.

#### **Response 409 Conflict:**

```json
{ "error": "USER_ALREADY_EXISTS", "message": "Ya existe un usuario con ese email", "timestamp": "2026-10-05T10:30:00Z" }
```

#### **2. POST /api/auth/login**

**Acceso:** Público

**Descripción:** Autentica al usuario y devuelve un token JWT.

#### **Body:**

```json
{ "email": "carlos@test.com", "password": "Pass123!" }
```

#### **Response 200 OK:**

```json
{ "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", "tokenType": "Bearer", "expiresIn": 3600, "userId": 1, "role": "ADMIN" }
```

#### **Response 401 Unauthorized:**

```json
{ "error": "INVALID_CREDENTIALS", "message": "Email o contraseña incorrectos", "timestamp": "2026-10-05T10:30:00Z" }
```

#### **Validaciones:**

1. Buscar usuario por email.
2. Verificar contraseña (bcrypt compare).
3. Si éxito: generar token JWT firmado con `jwt.secret`. El token debe contener los claims `sub` (id usuario), `username` y `role`.
4. Si fallo: lanzar excepción `RuntimeException("Credenciales inválidas")`.

### **Implementación de seguridad**

Se usa `@PreAuthorize("hasRole('JEFE_TALLER')")` o lógica manual en el servicio para restringir los endpoints de administración de catálogos. Para obtener el usuario autenticado se recomienda usar `SecurityContextHolder` (Spring Boot 4/Jakarta). Como `Principal.getName()` suele devolver el username y no el rol, el rol debe pasarse desde el filtro o consultarse mediante un servicio `SecurityContextService`.

### **Modelo de datos — SEC**

#### **Usuario**

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| id | Long | Identificador único |
| username | String | Nombre de usuario (único) |
| password | String | Hash bcrypt de la contraseña |
| activo | Boolean | Indica si la cuenta está activa |
| rolId | Long | Rol del usuario (referencia a Rol) |

#### **Rol**

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| id | Long | Identificador único |
| nombre | String | Nombre del rol (único) |

### **Enums — SEC**

UserRole: ADMIN, JEFE_TALLER, MECANICO

## **Módulo Gestión de Productos — M100**

### **Descripción**

Mantiene el catálogo de productos del taller junto con sus categorías y proveedores. Es la fuente de verdad que consultan M200, M300 y M400. El stock de un producto no se edita desde este módulo: solo cambia mediante movimientos de inventario.

### **Reglas de negocio**

| **Regla** | **Descripción** |
|---|---|
| RN101 | El campo `codigo` debe ser único. Validar antes de guardar |
| RN102 | `nombre` y `categoria` son obligatorios; no se guarda si están vacíos. La categoría debe existir y estar activa para asociarse a un producto |
| RN103 | `unidadMedida` es obligatoria y principal |
| RN104 | `stockMinimo` numérico >= 0 |
| RN105 | Cada producto debe tener un `proveedorPrincipal` activo |
| RN106 | El stock inicial de un producto nuevo es **0**. No se permite editar el stock desde el formulario de producto (solo por movimientos) |
| RN108 | Los productos con movimientos históricos (registros en `movimiento_inventario`) **no** se pueden eliminar físicamente. Solo desactivación lógica (`activo = false`) |
| RN109 | El RUC del proveedor (`ruc` o código de identificación) debe ser único |

### **Endpoints requeridos**

#### **1. POST /api/productos**

**Acceso:** JEFE_TALLER (RN107)

**Descripción:** Da de alta un producto. Al crear, se setea `stockActual = 0` y `activo = true`.

#### **Body:**

```json
{ "codigo": "FILT-01", "nombre": "Filtro Aceite", "descripcion": "...", "unidadMedida": "UND", "stockMinimo": 10, "id_categoria": 1, "id_proveedor": 1 }
```

**Response 201 Created:** el producto creado.

#### **Validaciones:**

- Validar que `codigo` no exista (`SELECT count(*) = 0`).
- Validar que `id_categoria` y `id_proveedor` existan y estén `activo=true`. Si no, error 400 "Categoría inactiva" o "Proveedor inactivo".

#### **2. GET /api/productos**

**Acceso:** Usuarios autenticados

**Descripción:** Lista paginada de productos. Muestra `stockActual` y un indicador `stockCritico` (`stockActual <= stockMinimo`).

#### **Query params:**

- `page` y `size` (paginación)
- `nombre` (string, opcional) — búsqueda parcial insensible a mayúsculas/minúsculas
- `categoria` (opcional)

#### **3. PUT /api/productos/{id}**

**Acceso:** JEFE_TALLER (RN107)

**Descripción:** Actualiza un producto existente.

#### **Validaciones:**

- Validar que el producto exista.
- **Reglas RN106 y RN108:** si el producto tiene registros en `movimiento_inventario` (historial), **no permitir** editar campos que afecten al stock o la desactivación directa. Solo permitir editar `nombre`, `descripcion` y `unidadMedida`. Si se intenta cambiar `stockMinimo` o `activo` y hay historial, lanzar excepción.
- Si no hay historial, permitir la edición total.

#### **4. PATCH /api/productos/{id}/desactivar**

**Acceso:** JEFE_TALLER (RN107)

**Descripción:** Desactivación lógica del producto. Reemplaza al `DELETE /api/productos/{id}` (opcional, ya que RN108 indica que no se eliminan físicamente).

#### **Validaciones:**

- RN108: si hay movimientos, lanzar error: "No se puede desactivar: el producto tiene movimientos registrados. Use la opción de desactivación lógica."
- Si no hay movimientos, setear `activo = false`.

#### **5. POST /api/categorias**

**Acceso:** JEFE_TALLER (RN107)

**Descripción:** Da de alta una categoría. Se gestiona desde `CategoriaController`.

#### **Validaciones:**

- El `nombre` no puede estar vacío.
- El `nombre` debe ser único.

#### **6. POST /api/proveedores**

**Acceso:** JEFE_TALLER (RN107)

**Descripción:** Da de alta un proveedor. Se gestiona desde `ProveedorController`.

#### **Validaciones:**

- El `ruc` (o código de identificación) debe ser único (RN109). Si ya existe, error `409`.

### **Modelo de datos — M100**

#### **Categoria**

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| id | Long | Identificador único |
| nombre | String | Nombre de la categoría |
| descripcion | String | Descripción |
| activo | Boolean | Indica si está activa |

#### **Proveedor**

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| id | Long | Identificador único |
| ruc | String | Registro único de la empresa |
| razonSocial | String | Razón social |
| contacto | String | Persona de contacto |
| telefono | String | Teléfono de contacto |
| correo | String | Correo electrónico |
| direccion | String | Dirección |
| activo | Boolean | Indica si está activo |

#### **Producto**

Entidad compartida por M200, M300 y M400.

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| id | Long | Identificador único |
| codigo | String | Código único del producto |
| sku | String | Código único del producto (SKU) |
| nombre | String | Nombre del producto |
| descripcion | String | Descripción |
| unidadMedida | String | Unidad principal |
| stockActual | Integer | Stock actual en sistema |
| stockMinimo | Integer | Mínimo configurado |
| costoUltimaCompra | BigDecimal | Costo de la última compra |
| activo | Boolean | Estado lógico |
| categoriaId | Long | Categoría (referencia a Categoria) |
| proveedorId | Long | Proveedor principal (referencia a Proveedor) |

## **Módulo Gestión de Inventario — M200**

### **Descripción**

Registra los movimientos de stock del taller: entradas de productos, consumos de los mecánicos y ajustes por conteo físico. Consume M100 para validar productos y proveedores, y para actualizar el stock y el costo de última compra. Cada movimiento conserva tipo, producto, cantidad o variación, stock anterior y posterior, fecha, usuario y motivo (RN208).

### **Reglas de negocio**

| **Regla** | **Descripción** |
|---|---|
| RN201 | El Jefe de taller (rol `JEFE_TALLER`) registra la entrada con producto, proveedor, cantidad y costo unitario. El sistema asigna fecha y usuario automáticamente |
| RN202 | Cantidad > 0. Costo unitario >= 0. El proveedor debe estar activo |
| RN203 | Al confirmar, el sistema incrementa `stockActual` del producto y actualiza `costoUltimaCompra` con el costo unitario de esa entrada |
| RN204 | El mecánico registra los productos utilizados directamente. No crea solicitud al Jefe |
| RN205 | Cada consumo identifica al mecánico autenticado, fecha, productos, cantidades y finalidad. La referencia a vehículo/cliente es opcional |
| RN206 | Cada producto consumido debe existir, estar activo y tener stock suficiente. Toda cantidad > 0 y en su unidad de medida. **No stock negativo:** cualquier operación que descuente stock debe verificar que el resultado no sea < 0 |
| RN207 | Consumo atómico: si falla algún producto, **no** se descuenta nada parcial |
| RN208 | Cada movimiento confirmado conserva tipo, producto, cantidad/variación, stock anterior y posterior, fecha y usuario, más motivo/justificación |
| RN209 | Solo el Jefe de taller puede ajustar. Cantidad física >= 0. Justificación obligatoria. Los ajustes no modifican el costo de última compra |

### **Endpoints requeridos**

#### **1. POST /api/movimientos/entrada**

**Acceso:** JEFE_TALLER

**Descripción:** Registra la entrada de un producto. Datos requeridos: producto, proveedor, cantidad y costo unitario (el documento original no define el JSON). La fecha y el usuario los asigna el sistema.

#### **Validaciones:**

1. **Autorización:** verificar que el usuario autenticado tenga rol `JEFE_TALLER`. Si no, `403`.
2. **Existencias:** buscar producto por ID. Si no existe o `activo=false`, error 404/400.
3. **Proveedor:** buscar proveedor por ID. Si no existe o `activo=false`, error 400.
4. **Cantidad:** el entero debe ser `> 0`. Si es 0 o negativo, error 400 "Cantidad debe ser mayor que cero".
5. **Costo:** debe ser `>= 0`. Si es negativo, error 400 "Costo unitario no puede ser negativo".
6. **Lógica de stock (RN203):**
   - Leer `stockActual` actual.
   - Calcular `stockPosterior = stockActual + cantidad`.
   - Actualizar la entidad `Producto` con el nuevo `stockActual` y `costoUltimaCompra = costoUnitario` (sobrescribe el costo anterior; los consumos y ajustes no modifican ese costo).
   - Registrar `MovimientoInventario` con tipo `ENTRADA`, `stockAnterior` (viejo), `stockPosterior` (nuevo), `costoUnitario`, fecha actual y usuario autenticado.

**Response 201 Created:** el movimiento creado y el nuevo estado del producto (stock actualizado).

#### **2. GET /api/existencias**

**Acceso:** Usuarios autenticados

**Descripción:** Consulta las existencias (stock actual) de un producto o de un conjunto de productos.

#### **Query params:**

- `ids` (lista de ids de producto, requerido) — ej: `?ids=1,3,5`. Para consultar un solo producto se envía un único id.

#### **Response 200 OK (ejemplo):**

```json
[
  { "productoId": 1, "codigo": "FILT-01", "nombre": "Filtro Aceite", "stockActual": 2, "stockMinimo": 10, "stockCritico": true }
]
```

#### **Validaciones:**

- `ids` es obligatorio; si falta o está vacío, error 400.
- Si algún producto no existe, error 404; si está inactivo, error 400.
- Retornar, por cada producto, `stockActual`, `stockMinimo` y un indicador `stockCritico`.
- RN104: el stock actual se expone en el campo `stockActual` del producto. RN302: si `stockActual <= stockMinimo`, el producto se considera en alerta de bajo stock.

#### **3. POST /api/consumos**

**Acceso:** MECANICO (también se permite JEFE_TALLER, principalmente para pruebas)

**Descripción:** Registra el consumo de productos por parte del mecánico, descontando el stock de forma atómica.

#### **Body:**

```json
{
  "finalidad": "Cambio de aceite",
  "items": [
    { "productoId": 1, "cantidad": 2 },
    { "productoId": 3, "cantidad": 1 }
  ],
  "referenciaVehiculoCliente": "ABC-123" // Opcional
}
```

#### **Validaciones por ítem (iterar la lista):**

- Buscar producto. Si no existe o `activo=false`, lanzar error identificando el producto (ej: "Producto ID 99 no encontrado o inactivo").
- Validar cantidad > 0.
- Validar stock suficiente (`producto.stockActual >= cantidad`). Si falla uno, no continuar.

#### **Lógica atómica (RN207):**

- **Opción A (más segura):** validar TODOS los ítems primero en un bucle. Si todos pasan, entrar en una transacción (`@Transactional`) para descontar el stock de todos y guardar el consumo.
- **Opción B (en el Service):** usar `@Transactional`. Intentar descontar el stock del primer producto. Si falla (excepción personalizada `StockInsuficienteException`), hacer rollback completo de la transacción, de modo que ningún stock se descuenta y el consumo no se guarda.
- *Decisión:* usar `@Transactional` + validación previa + descuento en cadena dentro de la transacción.

#### **Guardado:**

- Guardar la entidad `Consumo` (con `fecha`, `finalidad`, `id_usuario` = mecánico, `referenciaVehiculoCliente`).
- Para cada ítem, guardar un registro en `movimiento_inventario` con tipo `CONSUMO`, restar `cantidad` del `stockActual` del producto y guardar `stockAnterior`/`stockPosterior`.

**Errores:** `StockInsuficienteException` debe ser capturada en el controlador y devuelta al cliente con HTTP 400 y un mensaje claro que indique qué producto no tiene suficiente stock.

#### **4. POST /api/movimientos/ajuste**

**Acceso:** JEFE_TALLER

**Descripción:** Registra un ajuste de inventario a partir de un conteo físico.

#### **Body:**

```json
{
  "productoId": 5,
  "cantidadFisica": 15, // Conteo físico realizado
  "justificacion": "Inventario inicial" o "Detección de robo"
}
```

#### **Validaciones:**

1. **Autorización:** verificar rol `JEFE_TALLER`.
2. Buscar producto. Si no existe o está inactivo, error.
3. Obtener el `stockActual` actual.
4. **Regla RN209:** calcular `variacion = cantidadFisica - stockActual`.
5. Validar que `cantidadFisica >= 0` (aunque el tipo sea int, validar en servicio).
6. Si `variacion` es positiva (ganancia) o negativa (pérdida), se actualiza: `stockActual` del producto pasa a `cantidadFisica`.
7. Registrar `MovimientoInventario` con tipo `AJUSTE`, `cantidad` (podría ser null o la variación), `variacion`, `stockAnterior` (viejo), `stockPosterior` (nuevo = cantidadFisica), `motivo` = justificación, usuario = Jefe.

**Notas:** RN209: "Los ajustes no modifican el costo de última compra". A diferencia de la entrada, el costo unitario del movimiento de ajuste puede ser 0 o el costo actual, pero no se actualiza el campo `costoUltimaCompra` del producto.

### **Lógica interna del módulo**

#### **Descuento de stock (reutilizable)**

- Implementar un método `descontarStock(int cantidadASumar)` en `Producto`.
- Si `stockActual - cantidadASumar < 0`, lanzar `IllegalArgumentException("Stock insuficiente. Quedaría negativo.")`.
- Este método lo usa el registro de consumos y debe ser reutilizable.
- Para entradas no hay riesgo de negativo (se suma), pero se debe validar que la cantidad sea positiva.

#### **Validación y descuento atómico**

Método de servicio `validarYDescontarStock(List<ConsumoItem> items)` con `@Transactional` (RN207: todo o nada). El flujo es:

1. **Validación previa** de cada ítem, para dar un mensaje de error claro: el producto debe existir (`ProductoNoEncontradoException`), estar activo (`ProductoInactivoException`) y tener stock suficiente (`StockInsuficienteException`, con el producto, la cantidad pedida y el stock actual).
2. **Aplicación de cambios** dentro de la transacción: crear el `Consumo` con su finalidad y el mecánico y, para cada ítem, obtener el producto con bloqueo (`findByIdForUpdate`, optimista o pesimista), restar la cantidad de `stockActual` y registrar el movimiento de tipo `CONSUMO`.
3. Si ocurre algún fallo a pesar de la validación previa (por ejemplo, una condición de carrera), `@Transactional` hace rollback completo. La validación previa cubre el caso de negocio normal.

### **Modelo de datos — M200**

Usa la entidad `Producto` (ver M100) y `Usuario` (ver SEC).

#### **Consumo**

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| id | Long | Identificador único |
| fecha | Instant | Fecha del consumo |
| finalidad | String | Finalidad o justificación |
| referenciaVehiculoCliente | String | Referencia del vehículo del cliente |
| usuarioId | Long | Usuario que registró el consumo |

#### **MovimientoInventario**

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| id | Long | Identificador único |
| fecha | Instant | Fecha del movimiento |
| tipo | Enum | Tipo de movimiento (ENTRADA, CONSUMO, AJUSTE) |
| cantidad | Integer | Cantidad (entradas y consumos) |
| variacion | Integer | Variación (ajustes) |
| stockAnterior | Integer | Stock anterior |
| stockPosterior | Integer | Stock posterior |
| costoUnitario | BigDecimal | Costo de la entrada |
| motivo | String | Finalidad o justificación |
| productoId | Long | Producto (referencia a Producto) |
| consumoId | Long | Consumo (si aplica) |
| usuarioId | Long | Usuario que registró |

### **Enums — M200**

TipoMovimiento: ENTRADA, CONSUMO, AJUSTE

## **Módulo Control y Abastecimiento — M300**

### **Descripción**

Gestiona el stock mínimo de los productos y calcula las alertas de bajo stock. Consume M100 para leer productos activos y su proveedor principal. Las alertas no se persisten: son una vista derivada que se calcula a partir del stock actual y del stock mínimo.

### **Reglas de negocio**

| **Regla** | **Descripción** |
|---|---|
| RN104 | Stock mínimo numérico >= 0 |
| RN301 | Cada producto activo tendrá un stock mínimo configurado |
| RN302 | Se considerará bajo stock cuando `stock_actual <= stock_minimo` |
| RN303 | La alerta mostrará como mínimo código, nombre, stock actual, stock mínimo y proveedor principal |
| RN304 | Las alertas se recalculan después de cada movimiento (cambiar el mínimo afecta la alerta inmediata) |
| RN305 | La cantidad sugerida es la diferencia `stock_minimo - stock_actual` (solo referencial) |

### **Endpoints requeridos**

#### **1. PUT /api/productos/{id}/stock-minimo**

**Acceso:** JEFE_TALLER

**Descripción:** Configura el stock mínimo de un producto.

#### **Body:**

```json
{ "nuevoStockMinimo": 10 }
```

#### **Validaciones:**

- El producto debe existir.
- El nuevo valor debe ser un entero >= 0. Si no, error 400.
- Actualizar el campo `stock_minimo` del producto.
- **RN304:** después de guardar, el sistema debe "disparar" una revisión de alerta. Como no hay un evento real-time complejo, se puede simplemente retornar el nuevo estado y que el frontend o un servicio separado recompute; o bien guardar y que el próximo consumo/entrada recalcule.

**Response 200 OK:** el producto actualizado con el nuevo `stockMinimo`.

#### **2. GET /api/alertas** (o `GET /api/productos/alertas`)

**Acceso:** Usuarios autenticados

**Descripción:** Retorna la lista calculada por `AlertaService` (generación automática de alertas), incluyendo el nombre del proveedor principal (dato que viene de la relación `Producto -> Proveedor`).

**Response 200 OK:** la lista de objetos `AlertaDTO`, o lista vacía si no hay alertas.

### **Generación automática de alertas**

Se implementa con el servicio `AlertaService` y un scheduled task o trigger (ejecución diaria) que revise el stock. No hace falta una UI completa de "gestionar alertas" si el enfoque es el backend, pero sí el servicio que las calcula. La lógica es:

1. Consultar los productos activos donde `stockActual <= stockMinimo`.
2. Para cada uno, tomar el nombre del proveedor principal (razón social o contacto), si existe.
3. Calcular la cantidad sugerida `stockMinimo - stockActual` (RN305) y generar la alerta solo si es mayor que 0.
4. Retornar la lista de `AlertaStock` con código, nombre, stock actual, stock mínimo, cantidad sugerida y proveedor.

Si la lista está vacía, el sistema informa que "No hay productos con bajo stock" (RN302, flanco positivo).

### **Modelo de datos — M300**

Usa la entidad `Producto` (ver M100). Según el documento original, la alerta no se almacena como entidad independiente: se implementa como una clase de retorno o lista calculada en memoria/servicio. Si se quisiera persistir para historial, se crearía la tabla `alerta_stock`.

#### **AlertaStock (DTO, no persistente)**

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| codigo | String | Código del producto |
| nombre | String | Nombre del producto |
| stockActual | Integer | Stock actual |
| stockMinimo | Integer | Stock mínimo configurado |
| cantidadSugerida | Integer | `stockMinimo - stockActual` (referencial, RN305) |
| proveedor | String | Proveedor principal del producto |

## **Módulo Consultas y Reportes — M400**

### **Descripción**

Expone reportes de solo lectura, exportación a PDF y Excel, un dashboard con resumen ejecutivo y estadísticas de movimientos. Consume `movimiento_inventario` (M200), las alertas (M300), los productos (M100) y los usuarios (SEC).

### **Reglas de negocio**

| **Regla** | **Descripción** |
|---|---|
| RN404 | El reporte de consumo permite identificar producto, cantidad utilizada, fecha, mecánico y finalidad declarada |
| RN405 | Los filtros por fecha validarán que el rango sea correcto y que la fecha inicial no sea posterior a la final |
| RN407 | El valor estimado es la suma de `stock_actual * costo_ultima_compra`. Es una estimación operativa, no contable oficial |
| RN408 | Los reportes usarán datos actuales y movimientos confirmados, serán de solo lectura y conservarán la consulta histórica aunque un producto esté inactivo. Si un producto no tiene costo de última compra registrado, se identifica y se excluye del cálculo total, informando la cantidad de productos excluidos |

### **Endpoints requeridos**

#### **1. GET /api/reportes/movimientos**

**Acceso:** Usuarios autenticados

**Descripción:** Reporte de movimientos por periodo.

#### **Query params:**

- `desde` y `hasta` (fecha, requeridos) — ej: `?desde=2023-01-01&hasta=2023-12-31`
- `tipo` (enum, opcional) — ENTRADA, CONSUMO o AJUSTE

#### **Validaciones:**

1. Parámetros `desde` y `hasta` obligatorios.
2. Validar `hasta >= desde`. Si no, error 400 "La fecha final no puede ser anterior a la inicial".
3. Consultar la tabla `movimiento_inventario`.
4. Filtrar por `fecha` entre `desde` y `hasta` (inclusive).
5. Filtrar opcionalmente por `tipo`.
6. Retornar una lista o un objeto de resumen con totales por tipo.

#### **Response 200 OK (ejemplo):**

```json
{
  "totalEntradas": 150,
  "totalConsumos": 80,
  "totalAjustes": 5,
  "movimientos": [ /* lista de objetos movimiento DTO */ ]
}
```

#### **2. GET /api/reportes/consumo**

**Acceso:** Usuarios autenticados

**Descripción:** Reporte de consumo por periodo (`?desde=&hasta=`).

#### **Implementación:**

- Consultar `movimiento_inventario` donde `tipo = 'CONSUMO'`.
- Unir con la tabla `usuario` para obtener el nombre del mecánico.
- Agrupar por producto y mecánico para sumar cantidades.
- Retornar lista: `{ "productoNombre": "Aceite", "mecanico": "Juan", "totalKgs": 50, "fechas": ["2023-01-01"] }`.

#### **3. GET /api/reportes/bajo-stock**

**Acceso:** Usuarios autenticados

**Descripción:** Agrupa los productos en alerta (vista de M300) según su proveedor principal.

#### **Implementación:**

- Llamar a la lógica de alertas de M300.
- Agrupar el resultado por `proveedorPrincipal.nombre` o `ruc`.
- Mostrar para cada proveedor: lista de sus productos en alerta, stock actual, mínimo y sugerido.

#### **Response 200 OK (ejemplo):**

```json
{
  "proveedor": "Distribuidor Los Pinos",
  "productos": [
    { "codigo": "FILT-01", "stockActual": 2, "stockMinimo": 10, "sugerido": 8 }
  ]
}
```

#### **4. GET /api/reportes/valor-inventario**

**Acceso:** Usuarios autenticados

**Descripción:** Valor estimado del inventario (RN407, RN408).

#### **Implementación:**

- Consultar todos los productos activos.
- Iterar:
  - Si `producto.costoUltimaCompra != null` y `producto.stockActual > 0`: `valorParcial = producto.stockActual * producto.costoUltimaCompra` y acumular en el total.
  - Si `producto.costoUltimaCompra == null`: contar en un contador `productosSinCosto`.

#### **Response 200 OK:**

- `totalGeneral`
- `totalProductosConCosto`
- `productosSinCosto` (cantidad)
- Lista detalle: `producto.codigo`, `producto.nombre`, `stockActual`, `costoUltimaCompra` (o null), `valorEstimadoProducto` (o 0).

#### **5. Exportación de reportes a PDF y Excel**

**Acceso:** Usuarios autenticados

**Descripción:** Los endpoints de reportes de movimientos, consumo y bajo stock deben tener una variante o parámetro `formato=pdf|excel`. El contenido debe ser el mismo mostrado en pantalla, pero formateado. Ejemplo: `GET /api/reportes/movimientos/exportar?formato=pdf` debe generar un archivo descargable.

Requiere las dependencias `itextpdf` o `poi` (Apache POI) en `pom.xml`. La implementación exacta de PDF/Excel depende de la librería elegida.

#### **6. GET /api/dashboard**

**Acceso:** JEFE_TALLER y MECANICO

**Descripción:** Resumen ejecutivo. Agrega datos de los módulos internos y retorna un DTO de Dashboard que incluye:

- `fleet`: total de vehículos, disponibles, en mantenimiento, etc. (de US-08 / M200).
- `kpis`: rutas completadas, distancia total, costo de combustible, costo de mantenimiento (de US-17/18/20).
- `alertas`: cantidad total de alertas abiertas (de US-15/16).
- `upcomingMaintenance`: las 5 próximas órdenes de mantenimiento pendientes (de US-13).

#### **7. GET /api/estadisticas**

**Acceso:** Usuarios autenticados

**Descripción:** Series temporales de movimientos. Similar a los `/stats` del contexto FleetControl, adaptado a este inventario: devuelve la cantidad de movimientos por periodo.

#### **Query params:**

- `from` y `to` (fecha, requeridos)
- `granularity` (enum) — `DAY`, `WEEK` o `MONTH`

#### **Implementación básica:**

- Consultar `movimiento_inventario`.
- Si `granularity = DAY`: agrupar por fecha (`fecha`).
- Devolver mapa `fecha` -> `cantidad de movimientos`.

### **Modelo de datos — M400**

Este módulo no define entidades propias. Consulta `MovimientoInventario` y `Consumo` (ver M200), `Producto` (ver M100) y `Usuario` (ver SEC).

## **Módulo Transversal: Documentación y Pruebas Unitarias**

### **Descripción**

Agrupa las tareas que afectan a todo el sistema: el README y Swagger (US-26) y el criterio de pruebas unitarias, que no es una historia aparte sino que se reparte entre las historias que tocan una regla de negocio.

### **Pruebas unitarias**

- Se escriben con JUnit 5 y Mockito.
- Cubren las reglas críticas del negocio (RN101 a RN209), con un mínimo de 5 tests por regla crítica.
- Cada historia que toca una regla crea, en la misma historia, los tests unitarios de esa regla (ver el backlog).

### **README y Swagger**

**`README.md` en la raíz**, con este contenido:

- Descripción general del proyecto (Monolito modular Java 21 / Spring Boot 4).
- Requisitos previos: Java 21, Maven, PostgreSQL 15.
- Guía de arranque:
  1. Clonar repositorio.
  2. Crear archivo `.env` con las variables obligatorias (ver sección de variables de entorno).
  3. Ejecutar `mvn spring-boot:run` o `docker compose up` (si se usa docker).
- Lista de variables de entorno obligatorias y opcionales.
- Endpoints principales (documentación Swagger automática) y uso de la cabecera `X-API-Version`.
- Instrucciones para correr tests: `mvn test`.
- Diagrama simple de la arquitectura (Capas: Presentación -> Negocio -> Datos).
- Información de contacto o autor.

**Swagger UI:**

- Al levantar la aplicación, se accede a `http://localhost:8080/swagger-ui.html`.
- Los endpoints deben estar documentados con las anotaciones `@ApiOperation`, `@ApiParam` y `@ApiResponse` en los controladores, o con la configuración estándar de Springdoc / Springfox que venga por defecto con Spring Boot 4, para que la documentación esté viva y actualizada con los códigos de error (400, 401, 403, 409, 500).

## **Comunicación entre módulos**

Los módulos se ejecutan en el mismo proceso y se comunican mediante llamadas a sus servicios, sin llamadas HTTP entre ellos. Un único `JwtAuthenticationFilter` valida el JWT de todas las peticiones externas.

### **Autenticación**

- **Peticiones externas:** cabecera `Authorization: Bearer <token>`. El filtro verifica la firma HS256 con `jwt.secret`, lee los claims `sub`, `username` y `role`, y usa el rol para decidir el acceso.
- **Endpoints públicos:** `POST /api/auth/register` y `POST /api/auth/login`.
- **Clave interna:** la variable `INTERNAL_API_KEY` (`internal.api.key`) es obligatoria y se valida al arrancar en `AppConfig`. El documento original no describe dónde se usa.

### **Mapa de dependencias**

| **Módulo que llama** | **Módulos a los que llama** |
|---|---|
| M400 | M100, M200, M300, SEC |
| M300 | M100 |
| M200 | M100 |
| SEC, M100 | — |

### **Llamadas entre módulos**

| **Acción** | **Módulo que la inicia** | **Llamadas que desencadena** |
|---|---|---|
| Crear un producto | M100 | M100: valida que la categoría y el proveedor existan y estén activos |
| Registrar una entrada | M200 | M100: valida producto y proveedor activos; actualiza `stockActual` y `costoUltimaCompra` |
| Registrar un consumo | M200 | M100: valida productos y stock; descuenta `stockActual` de cada producto dentro de una transacción |
| Registrar un ajuste | M200 | M100: valida el producto y actualiza `stockActual` |
| Consultar alertas | M300 | M100: productos activos con su proveedor principal |
| Reporte de bajo stock | M400 | M300: lógica de alertas, agrupada por proveedor |
| Reporte de consumo | M400 | M200: movimientos de tipo CONSUMO; SEC: nombre del mecánico |
| Dashboard | M400 | Datos de los módulos internos (productos, movimientos, alertas) |

### **Contrato de errores común**

Todos los endpoints de la API (`/api`) devuelven los errores con esta estructura, como mínimo:

| **Campo** | **Tipo** | **Descripción** |
|---|---|---|
| `error` | String | Código del error (ej: `VALIDATION_ERROR`) |
| `message` | String | Mensaje descriptivo del error, sin información técnica sensible |
| `details` | Array | Detalle de errores por campo (validaciones). Cada elemento: `{ "field": "...", "reason": "..." }` |
| `endpoint` | String | Endpoint sobre el que ocurrió el error (ej: `/api/movimientos/entrada`) |
| `timestamp` | String (ISO 8601) | Momento de la ocurrencia del error (ej: `2026-10-05T10:30:00Z`) |

Ejemplo de respuesta (error de validación):

```json
{
  "error": "VALIDATION_ERROR",
  "message": "La petición contiene campos no válidos",
  "details": [
    {
      "field": "year",
      "reason": "debe estar entre 1990 y 2027"
    }
  ],
  "endpoint": "/api/movimientos/entrada",
  "timestamp": "2026-10-05T10:30:00Z"
}
```

| **HTTP** | **Código** | **Cuándo** |
|---|---|---|
| 400 | VALIDATION_ERROR | Algún campo no cumple las validaciones (manejo de `RuntimeException`); `StockInsuficienteException` en el registro de consumos |
| 401 | UNAUTHORIZED, INVALID_CREDENTIALS | Token inválido o caducado, o credenciales incorrectas |
| 403 | FORBIDDEN | El rol del usuario no tiene permiso |
| 404 | — | El recurso no existe |
| 409 | USER_ALREADY_EXISTS, STOCK_INSUFICIENTE | Conflicto con el estado actual (usuario duplicado, RUC duplicado; stock insuficiente según el handler global) |
| 500 | — | Error interno (documentado en Swagger) |

### **Manejo de excepciones global**

Dado que no hay valores por defecto y las validaciones son estrictas, es crucial tener un controlador de excepciones global (`GlobalExceptionHandler`, con `@RestControllerAdvice`, en `com.taller.config`):

- `RuntimeException` se responde con `400` y el código `VALIDATION_ERROR`, con el mensaje de la excepción.
- `StockInsuficienteException` (excepción personalizada) se responde con `409` y el código `STOCK_INSUFICIENTE`.
- Ambas respuestas siguen el contrato de errores común: `endpoint` lleva la URI de la petición y `timestamp` el momento del error.

### **Convenciones comunes**

- Todo el código y los identificadores están en inglés; las tablas y columnas usan snake_case en minúsculas.
- Los identificadores de las entidades son `Long`.
- Los instantes usan ISO-8601 (ej: `2026-10-05T10:30:00Z`).
- La paginación usa los parámetros `page` y `size`.
- Los endpoints usan el prefijo `/api`, recursos en plural y los verbos HTTP según su función; la versión se gestiona con API Versioning (cabecera `X-API-Version`).
- Toda entrada de usuario se valida en backend antes de procesarse.
- La documentación de los endpoints está en Swagger UI (`/swagger-ui.html`).

## **Configuración y variables de entorno**

El siguiente principio es transversal a todo el proyecto: **todas las configuraciones deben obtenerse exclusivamente de Variables de Entorno (Environment Variables). No se permiten valores por defecto en el código (`application.yml`) ni en las anotaciones `@Value`.** Si una variable crítica falta, la aplicación debe fallar en el arranque (Fail-Fast).

Todos los endpoints de la API usan el prefijo `/api` (por ejemplo, `/api/productos`, `/api/movimientos/entrada`) y se versionan con el **API Versioning nativo de Spring Framework 7, incorporado en Spring Boot 4**, usando versionado por cabecera HTTP (header-based): la versión se declara con el atributo `version` de cada anotación de mapeo y el cliente la envía en la cabecera configurada (`X-API-Version`). No se escribe `/v1` en las rutas y no se define una versión por defecto, en línea con el principio de no usar valores por defecto.

### **Estructura de `application.yml` (Spring Boot 4)**

En `src/main/resources/application.yml`, **no** se debe usar `default-value` en las propiedades. Se define la estructura, pero se espera que el entorno provea los valores.

```yaml
# application.yml
server:
  port: ${SERVER_PORT} # Obligatorio en entorno, no hay default

spring:
  # JDBC / JPA / Flyway
  datasource:
    url: ${DATASOURCE_URL}
    username: ${DATASOURCE_USERNAME}
    password: ${DATASOURCE_PASSWORD}
    hikari:
      # Configuración HikariCP
  jpa:
    hibernate:
      ddl-auto: validate # Importante: validate para no crear tablas automáticamente, usar Flyway
    show-sql: false
  flyway:
    baseline-on-migrate: true
  # API Versioning (Spring Boot 4)
  mvc:
    apiversion:
      supported: ${API_VERSIONS_SUPPORTED}
      use:
        header: ${API_VERSION_HEADER}
  # Configuración de seguridad JWT
  security:
    # Spring Security 6 configuración...

management:
  endpoints:
    web:
      exposure:
        include: health,info,beans # Solo los necesarios
```

### **Configuración de seguridad y JWT (Fail-Fast)**

La clase `AppConfig` (`com.taller.config`, Java 21) carga las propiedades `jwt.secret`, `jwt.expiration` e `internal.api.key` y las valida al arrancar (`@PostConstruct`). Si `jwt.secret` o `internal.api.key` son nulas o vacías, o si `jwt.expiration` es nula o menor o igual que 0, lanza `IllegalStateException` con un mensaje que empieza por "FATAL" y la aplicación no inicia. Expone un getter para cada propiedad.

### **Variables de entorno obligatorias (.env)**

El archivo `.env` (que debe estar en `.gitignore`) debe contener obligatoriamente:

```env
# Base de Datos
POSTGRES_URL=jdbc:postgresql://db:5432/inventario_taller
POSTGRES_USER=admin_taller
POSTGRES_PASSWORD=S3gur@W1nd3r

# Seguridad
JWT_SECRET=una_cadena_muy_segura_de_minimo_64_caracteres_aqui
JWT_EXPIRACION_SEGUNDOS=3600
INTERNAL_API_KEY=clave_secreta_interna_modulos

# API Versioning
API_VERSION_HEADER=X-API-Version
API_VERSIONS_SUPPORTED=1.0

# Aplicación
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

### **Migraciones Flyway y versionado**

Dado que no hay `ddl-auto: update` (se usa `validate`), todas las estructuras de tablas y cambios de esquema deben gestionarse mediante archivos de migración Flyway (`V1__base.sql`, `V2__añade_campo_stock_minimo.sql`, etc.). Esto garantiza que el esquema de la base de datos `inventario_taller` evolucione de manera controlada y que el código Java 21 se mantenga sincronizado.

### **Seguridad de datos sensibles**

Las variables de entorno como `JWT_SECRET` y `POSTGRES_PASSWORD` **nunca** deben verse en el código fuente ni en el historial de Git. El archivo `.env` debe añadirse inmediatamente a `.gitignore`. En producción, estas variables deben configurarse en el entorno de despliegue (Heroku, AWS, VPS, etc.) y no en el repositorio.

## **Historias de usuario**

Las historias de usuario se agrupan por módulo, de acuerdo con el alcance funcional del sistema. US-09 y US-25 se descartaron, US-10 (tests unitarios) se distribuyó entre las demás historias y el documento original no tiene US-24.

### **Criterio de pruebas unitarias**

- Toda historia que toca una regla de negocio (RN) crea, en esa misma historia, los tests unitarios de esa regla (JUnit 5 + Mockito).
- Mínimo 5 tests por regla crítica.
- Las historias sin reglas de negocio no crean tests unitarios de reglas.

### **Distribución de carga del equipo**

El equipo es de 4 personas. El reparto propuesto busca que la carga de trabajo sea similar entre los integrantes y conviene ajustarlo con el equipo.

| **Integrante** | **Historias** |
|---|---|
| Dev 1 | US-01, US-02, US-14, US-15, US-22, US-26 |
| Dev 2 | US-03, US-04, US-13, US-16, US-17 |
| Dev 3 | US-05, US-06, US-08, US-19, US-20, US-23 |
| Dev 4 | US-07, US-11, US-12, US-18, US-21 |

### **Backlog**

La columna "Depende de" se dedujo de lo que cada historia consume. La columna "Tests unitarios de" indica las reglas cuyos tests crea la historia.

| **ID** | **Historia** | **Módulo** | **Responsable** | **Depende de** | **Tests unitarios de** |
|---|---|---|---|---|---|
| US-01 | Infraestructura base y proyecto Spring Boot | SEC | Dev 1 | — | — |
| US-02 | Registro e inicio de sesión (Authentication) | SEC | Dev 1 | US-01 | RN107, RN109 |
| US-03 | Validación de JWT, roles y permisos | SEC | Dev 2 | US-02 | RN107 |
| US-04 | Catálogo de productos: alta, consulta y edición | M100 | Dev 2 | US-03 | RN101–RN106, RN108 |
| US-05 | Gestión de categorías y proveedores | M100 | Dev 3 | US-03 | RN102, RN105, RN109 |
| US-06 | Registro de entradas de productos | M200 | Dev 3 | US-04, US-05 | RN201–RN203 |
| US-07 | Validación de stock y actualización automática | M200 | Dev 4 | US-04 | RN206 |
| US-08 | Consulta de existencias | M200 | Dev 3 | US-04 | RN104, RN302 |
| US-11 | Registro de consumos por parte del mecánico | M200 | Dev 4 | US-04, US-07, US-12 | RN204–RN207 |
| US-12 | Validación de stock suficiente (atómico) | M200 | Dev 4 | US-07 | RN207 |
| US-13 | Registro de ajustes de inventario | M200 | Dev 2 | US-04 | RN208, RN209 |
| US-14 | Configuración de stock mínimo | M300 | Dev 1 | US-04 | RN104, RN301, RN304 |
| US-15 | Generación automática de alertas de bajo stock | M300 | Dev 1 | US-04, US-05 | RN302–RN305 |
| US-16 | Consulta de alertas y proveedor principal | M300 | Dev 2 | US-15 | — |
| US-17 | Reportes de movimientos por periodo | M400 | Dev 2 | US-06 | RN405, RN408 |
| US-18 | Reporte de consumo por periodo | M400 | Dev 4 | US-11 | RN404 |
| US-19 | Reporte de bajo stock agrupado por proveedor | M400 | Dev 3 | US-15 | — |
| US-20 | Consulta de valor estimado del inventario | M400 | Dev 3 | US-04 | RN407, RN408 |
| US-21 | Exportar reportes a PDF y Excel | M400 | Dev 4 | US-17, US-18, US-19 | — |
| US-22 | Dashboard con resumen ejecutivo | M400 | Dev 1 | US-16, US-20 | — |
| US-23 | Series temporales: estadísticas de movimientos | M400 | Dev 3 | US-06 | — |
| US-26 | Documentación y README final | Transversal | Dev 1 | — | — |

### **Módulo SEC — US-01 a US-03**

#### **US-01 — Infraestructura base y proyecto Spring Boot**

**Módulo:** SEC · **Responsable:** Dev 1 · **Reglas:** RN101 (referencia implícita en el setup; su test unitario se crea en US-04)

**Qué se debe crear:**

- Estructura de paquete `com.taller`.
- `pom.xml` (Maven) configurado para Java 21 y Spring Boot 4.
- Clase principal con `@SpringBootApplication`.
- Configuración inicial en `application.yml` que falle si no encuentra las variables de entorno críticas.
- Configuración de API Versioning (`spring.mvc.apiversion.*`) mediante variables de entorno.

#### **Criterios de aceptación:**

- El proyecto se levanta con `mvn spring-boot:run` o `java -jar`.
- Si faltan `jwt.secret` o `db.url`, la aplicación no inicia (validación en `AppConfig`).
- Si `AppConfig.validateEnvironment()` lanza excepción, la terminal muestra el mensaje de error y se detiene.
- Las rutas de los controladores no incluyen `/v1`: la versión se resuelve con API Versioning a partir de la cabecera `X-API-Version`.

#### **US-02 — Registro e inicio de sesión (Authentication)**

**Módulo:** SEC · **Responsable:** Dev 1 · **Reglas:** RN107, RN109 · **Depende de:** US-01

**Endpoints:** [`POST /api/auth/register`](#1-post-apiauthregister) · [`POST /api/auth/login`](#2-post-apiauthlogin)

**Qué se debe crear:**

- Entidad `Usuario` (relacionada con `Rol`).
- Controlador `AuthController`.
- Servicio de autenticación que use `jwt.secret` desde `AppConfig`.
- Endpoints `POST /api/auth/register` y `POST /api/auth/login`.
- Inserción del usuario `ADMIN` como usuario seed.
- Tests unitarios de RN107 y RN109.

#### **Criterios de aceptación:**

- Con email y username únicos, `POST /api/auth/register` crea el usuario; si ya existen, lanza `RuntimeException("Email already in use")` (`409 USER_ALREADY_EXISTS`).
- Una contraseña de menos de 8 caracteres, sin mayúscula o sin número se rechaza.
- Si no se especifica el rol, se asigna `MECANICO`.
- El usuario `ADMIN` existe como usuario seed y el rol `ADMIN` no se puede asignar mediante `POST /api/auth/register`.
- Con credenciales correctas, `POST /api/auth/login` devuelve un JWT con los claims `sub`, `username` y `role`; con credenciales incorrectas devuelve `401 INVALID_CREDENTIALS`.
- Existen los tests unitarios de RN107 y RN109.

#### **US-03 — Validación de JWT, roles y permisos**

**Módulo:** SEC · **Responsable:** Dev 2 · **Reglas:** RN107 · **Depende de:** US-02

**Qué se debe crear:**

- Filtro de seguridad `JwtAuthenticationFilter` (se ejecuta en cada petición).
- Anotaciones o lógica para verificar roles antes de ejecutar los endpoints.
- Tests unitarios de RN107.

#### **Criterios de aceptación:**

- Con un token inválido o caducado, se bloquea la petición y se devuelve `401 UNAUTHORIZED`.
- Con `role=MECANICO`, los endpoints `/api/productos/guardar`, `/api/categorias/guardar` y `/api/proveedores/guardar` devuelven `403 FORBIDDEN`.
- Si el usuario es `MECANICO` y hace `POST /api/vehiculos`, el controlador verifica el rol y rechaza.
- Existen los tests unitarios de RN107.

### **Módulo M100 — US-04 y US-05**

#### **US-04 — Catálogo de productos: alta, consulta y edición**

**Módulo:** M100 · **Responsable:** Dev 2 · **Reglas:** RN101–RN106, RN108 · **Depende de:** US-03

**Endpoints:** [`POST /api/productos`](#1-post-apiproductos) · [`GET /api/productos`](#2-get-apiproductos) · [`PUT /api/productos/{id}`](#3-put-apiproductosid) · [`PATCH /api/productos/{id}/desactivar`](#4-patch-apiproductosiddesactivar)

**Qué se debe crear:**

- Controlador `ProductoController`.
- Servicio `ProductoService`.
- Repositorio `ProductoRepository` (`JpaRepository`).
- Endpoints CRUD completos.
- Tests unitarios de RN101, RN102, RN103, RN104, RN105, RN106 y RN108.

#### **Criterios de aceptación:**

- `POST /api/productos` rechaza un `codigo` repetido, exige categoría y proveedor activos, y crea el producto con `stockActual = 0` y `activo = true` (`201 Created`).
- `GET /api/productos` es paginado, filtra por `nombre` (parcial, insensible a mayúsculas) o `categoria`, y muestra `stockActual` y `stockCritico`.
- `PUT /api/productos/{id}` valida que el producto exista y, con historial en `movimiento_inventario`, solo permite editar `nombre`, `descripcion` y `unidadMedida`; si intenta cambiar `stockMinimo` o `activo`, lanza excepción.
- `PATCH /api/productos/{id}/desactivar` rechaza la desactivación si hay movimientos registrados (RN108).
- Existen los tests unitarios de RN101 a RN106 y RN108.

#### **US-05 — Gestión de categorías y proveedores**

**Módulo:** M100 · **Responsable:** Dev 3 · **Reglas:** RN102, RN105, RN109 · **Depende de:** US-03

**Endpoints:** [`POST /api/categorias`](#5-post-apicategorias) · [`POST /api/proveedores`](#6-post-apiproveedores)

**Qué se debe crear:**

- Controladores `CategoriaController` y `ProveedorController`.
- Lógica de asociación.
- Tests unitarios de RN102, RN105 y RN109.

#### **Criterios de aceptación:**

- `POST /api/categorias` valida que el `nombre` no esté vacío y sea único.
- `POST /api/proveedores` valida que el `ruc` (o código de identificación) sea único; si ya existe, devuelve `409`.
- Al crear un producto, la categoría y el proveedor seleccionados deben estar activos; si no, error 400 "Categoría inactiva" o "Proveedor inactivo".
- Existen los tests unitarios de RN102, RN105 y RN109.

### **Módulo M200 — US-06, US-07, US-08, US-11, US-12 y US-13**

#### **US-06 — Registro de entradas de productos**

**Módulo:** M200 · **Responsable:** Dev 3 · **Reglas:** RN201–RN203 · **Depende de:** US-04, US-05

**Endpoint:** [`POST /api/movimientos/entrada`](#1-post-apimovimientosentrada)

**Qué se debe crear:**

- Controlador `MovimientoController` (o específico de entradas).
- Endpoint `POST /api/movimientos/entrada`.
- Tests unitarios de RN201, RN202 y RN203.

#### **Criterios de aceptación:**

- Con un usuario que no es `JEFE_TALLER`, se devuelve `403`.
- Con producto inexistente o inactivo, error 404/400; con proveedor inexistente o inactivo, error 400.
- Con cantidad 0 o negativa, error 400 "Cantidad debe ser mayor que cero"; con costo negativo, error 400 "Costo unitario no puede ser negativo".
- Al confirmar, `stockActual` aumenta, `costoUltimaCompra` se actualiza con el costo unitario de la entrada y se registra un `MovimientoInventario` tipo `ENTRADA` con `stockAnterior`, `stockPosterior`, fecha y usuario.
- La respuesta `201 Created` incluye el movimiento y el nuevo estado del producto.
- Existen los tests unitarios de RN201 a RN203.

#### **US-07 — Validación de stock y actualización automática**

**Módulo:** M200 · **Responsable:** Dev 4 · **Reglas:** RN206 (implícita) · **Depende de:** US-04

**Qué se debe crear:**

- Métodos auxiliares en `ProductoService` o `MovimientoService`.
- Lógica para evitar stock negativo.
- Tests unitarios de RN206.

#### **Criterios de aceptación:**

- `descontarStock(int cantidadASumar)` en `Producto` lanza `IllegalArgumentException("Stock insuficiente. Quedaría negativo.")` si el resultado es < 0.
- El método lo llama el registro de consumos y es reutilizable.
- Para entradas, la cantidad debe ser positiva.
- Existen los tests unitarios de RN206.

#### **US-08 — Consulta de existencias**

**Módulo:** M200 · **Responsable:** Dev 3 · **Reglas:** RN104, RN302 · **Depende de:** US-04

**Endpoint:** [`GET /api/existencias`](#2-get-apiexistencias)

**Qué se debe crear:**

- Endpoint `GET /api/existencias?ids=` para consultar las existencias (stock actual) de un producto o de un conjunto de productos.
- Lógica del indicador `stockCritico` (`stockActual <= stockMinimo`).
- Tests unitarios de RN104 y RN302.

#### **Criterios de aceptación:**

- Si `ids` falta o está vacío, se retorna error 400.
- Si algún producto no existe, se retorna error 404; si está inactivo, error 400.
- La respuesta incluye, por cada producto, `productoId`, `codigo`, `nombre`, `stockActual`, `stockMinimo` y el indicador `stockCritico` (`stockActual <= stockMinimo`).
- Un producto con `stockActual <= stockMinimo` se marca con `stockCritico = true` (RN302).
- Existen los tests unitarios de RN104 y RN302.

#### **US-11 — Registro de consumos por parte del mecánico**

**Módulo:** M200 · **Responsable:** Dev 4 · **Reglas:** RN204–RN207 · **Depende de:** US-04, US-07, US-12

**Endpoint:** [`POST /api/consumos`](#3-post-apiconsumos)

**Qué se debe crear:**

- Endpoint `POST /api/consumos`.
- Lógica para descontar stock de forma atómica.
- Tests unitarios de RN204, RN205, RN206 y RN207.

#### **Criterios de aceptación:**

- Solo el rol `MECANICO` (o `JEFE_TALLER` para pruebas) puede registrar consumos.
- Con un producto inexistente o inactivo, se devuelve un error que identifica el producto.
- Con cantidad <= 0 o stock insuficiente, no se continúa con el resto de ítems.
- Si todos los ítems son válidos, se guarda el `Consumo`, se registra un `MovimientoInventario` tipo `CONSUMO` por ítem y se descuenta el stock guardando `stockAnterior` y `stockPosterior`.
- Si falla algún producto, no se descuenta nada parcial.
- Existen los tests unitarios de RN204 a RN207.

#### **US-12 — Validación de stock suficiente (atómico)**

**Módulo:** M200 · **Responsable:** Dev 4 · **Reglas:** RN207 · **Depende de:** US-07

**Qué se debe crear:**

- Método de servicio `validarYDescontarStock(List<ConsumoItem> items)`.
- Uso de `@Transactional`.
- Tests unitarios de RN207.

#### **Criterios de aceptación:**

- El consumo es atómico: todo o nada.
- `StockInsuficienteException` se captura en el controlador y se devuelve al cliente con HTTP 400 y un mensaje claro que indica qué producto no tiene suficiente stock.
- Existen los tests unitarios de RN207.

#### **US-13 — Registro de ajustes de inventario**

**Módulo:** M200 · **Responsable:** Dev 2 · **Reglas:** RN208, RN209 · **Depende de:** US-04

**Endpoint:** [`POST /api/movimientos/ajuste`](#4-post-apimovimientosajuste)

**Qué se debe crear:**

- Endpoint `POST /api/movimientos/ajuste`.
- Tests unitarios de RN208 y RN209.

#### **Criterios de aceptación:**

- Solo `JEFE_TALLER` puede ajustar; la justificación es obligatoria y `cantidadFisica >= 0`.
- Con producto inexistente o inactivo, se devuelve error.
- Se calcula `variacion = cantidadFisica - stockActual`, se actualiza `stockActual` a `cantidadFisica` y se registra un `MovimientoInventario` tipo `AJUSTE` con `variacion`, `stockAnterior`, `stockPosterior` y `motivo`.
- El ajuste no modifica `costoUltimaCompra`.
- Existen los tests unitarios de RN208 y RN209.

### **Módulo M300 — US-14, US-15 y US-16**

#### **US-14 — Configuración de stock mínimo**

**Módulo:** M300 · **Responsable:** Dev 1 · **Reglas:** RN104, RN301, RN304 · **Depende de:** US-04

**Endpoint:** [`PUT /api/productos/{id}/stock-minimo`](#1-put-apiproductosidstock-minimo)

**Qué se debe crear:**

- Endpoint `PUT /api/productos/{id}/stock-minimo`.
- Tests unitarios de RN104, RN301 y RN304.

#### **Criterios de aceptación:**

- Solo `JEFE_TALLER` puede usarlo.
- Con `{ "nuevoStockMinimo": 10 }`, si el producto no existe o el valor no es un entero >= 0, se devuelve error (400 para el valor inválido).
- Se actualiza `stock_minimo` y se retorna el producto actualizado con el nuevo `stockMinimo`.
- Existen los tests unitarios de RN104, RN301 y RN304.

#### **US-15 — Generación automática de alertas de bajo stock**

**Módulo:** M300 · **Responsable:** Dev 1 · **Reglas:** RN302–RN305 · **Depende de:** US-04, US-05

**Qué se debe crear:**

- Servicio `AlertaService`.
- Un scheduled task o trigger que revise el stock.
- DTO `AlertaStock` (no persistente).
- Tests unitarios de RN302, RN303, RN304 y RN305.

#### **Criterios de aceptación:**

- Se genera una alerta por cada producto activo con `stock_actual <= stock_minimo`.
- Cada alerta muestra código, nombre, stock actual, stock mínimo, cantidad sugerida (`stock_minimo - stock_actual`) y proveedor principal.
- Si la lista está vacía, el sistema informa que "No hay productos con bajo stock".
- Existen los tests unitarios de RN302 a RN305.

#### **US-16 — Consulta de alertas y proveedor principal**

**Módulo:** M300 · **Responsable:** Dev 2 · **Depende de:** US-15

**Endpoint:** [`GET /api/alertas`](#2-get-apialertas-o-get-apiproductosalertas)

**Qué se debe crear:**

- Endpoint `GET /api/alertas` o `GET /api/productos/alertas`.

#### **Criterios de aceptación:**

- Retorna la lista calculada por el servicio de US-15, incluyendo el nombre del proveedor principal.
- Retorna `200` con lista vacía si no hay alertas, o la lista de objetos `AlertaDTO`.

### **Módulo M400 — US-17 a US-23**

#### **US-17 — Reportes de movimientos por periodo**

**Módulo:** M400 · **Responsable:** Dev 2 · **Reglas:** RN405, RN408 · **Depende de:** US-06

**Endpoint:** [`GET /api/reportes/movimientos`](#1-get-apireportesmovimientos)

**Qué se debe crear:**

- Endpoint `GET /api/reportes/movimientos?desde=2023-01-01&hasta=2023-12-31`.
- Tests unitarios de RN405 y RN408.

#### **Criterios de aceptación:**

- `desde` y `hasta` son obligatorios.
- Si `hasta` es anterior a `desde`, error 400 "La fecha final no puede ser anterior a la inicial".
- Se consulta `movimiento_inventario` entre `desde` y `hasta` (inclusive), con filtro opcional por `tipo`.
- Se retorna una lista o un objeto de resumen con totales por tipo.
- Existen los tests unitarios de RN405 y RN408.

#### **US-18 — Reporte de consumo por periodo**

**Módulo:** M400 · **Responsable:** Dev 4 · **Reglas:** RN404 · **Depende de:** US-11

**Endpoint:** [`GET /api/reportes/consumo`](#2-get-apireportesconsumo)

**Qué se debe crear:**

- Endpoint `GET /api/reportes/consumo?desde=&hasta=`.
- Tests unitarios de RN404.

#### **Criterios de aceptación:**

- Permite identificar producto, cantidad utilizada, fecha, mecánico y finalidad declarada.
- Consulta `movimiento_inventario` de tipo `CONSUMO`, une con `usuario` para obtener el nombre del mecánico y agrupa por producto y mecánico para sumar cantidades.
- Existen los tests unitarios de RN404.

#### **US-19 — Reporte de bajo stock agrupado por proveedor**

**Módulo:** M400 · **Responsable:** Dev 3 · **Depende de:** US-15

**Endpoint:** [`GET /api/reportes/bajo-stock`](#3-get-apireportesbajo-stock)

**Qué se debe crear:**

- Endpoint `GET /api/reportes/bajo-stock`.

#### **Criterios de aceptación:**

- Agrupa los productos en alerta (US-15) según su proveedor principal.
- Muestra por proveedor la lista de sus productos en alerta con stock actual, mínimo y sugerido.

#### **US-20 — Consulta de valor estimado del inventario**

**Módulo:** M400 · **Responsable:** Dev 3 · **Reglas:** RN407, RN408 · **Depende de:** US-04

**Endpoint:** [`GET /api/reportes/valor-inventario`](#4-get-apireportesvalor-inventario)

**Qué se debe crear:**

- Endpoint `GET /api/reportes/valor-inventario`.
- Tests unitarios de RN407 y RN408.

#### **Criterios de aceptación:**

- El valor estimado es la suma de `stock_actual * costo_ultima_compra` de los productos activos con costo y stock mayor que 0.
- Los productos sin costo de última compra se cuentan en `productosSinCosto` y se excluyen del total.
- La respuesta incluye `totalGeneral`, `totalProductosConCosto`, `productosSinCosto` y el detalle por producto.
- Existen los tests unitarios de RN407 y RN408.

#### **US-21 — Exportar reportes a PDF y Excel**

**Módulo:** M400 · **Responsable:** Dev 4 · **Depende de:** US-17, US-18, US-19

**Endpoint:** [Exportación de reportes a PDF y Excel (`formato=pdf|excel`)](#5-exportación-de-reportes-a-pdf-y-excel)

**Qué se debe crear:**

- Dependencias `itextpdf` o `poi` (Apache POI) en `pom.xml`.
- Endpoints o funcionalidad de descarga.

#### **Criterios de aceptación:**

- Los reportes de movimientos, consumo y bajo stock tienen una variante o parámetro `formato=pdf|excel`.
- El contenido es el mismo mostrado en pantalla, pero formateado.
- `GET /api/reportes/movimientos/exportar?formato=pdf` genera un archivo descargable.

#### **US-22 — Dashboard con resumen ejecutivo**

**Módulo:** M400 · **Responsable:** Dev 1 · **Depende de:** US-16, US-20

**Endpoint:** [`GET /api/dashboard`](#6-get-apidashboard)

**Qué se debe crear:**

- Endpoint `GET /api/dashboard`.

#### **Criterios de aceptación:**

- Agrega datos de los módulos internos y retorna un DTO de Dashboard con `fleet`, `kpis`, `alertas` y `upcomingMaintenance`.
- Solo pueden consultarlo los roles `JEFE_TALLER` y `MECANICO`.

#### **US-23 — Series temporales: estadísticas de movimientos**

**Módulo:** M400 · **Responsable:** Dev 3 · **Depende de:** US-06

**Endpoint:** [`GET /api/estadisticas`](#7-get-apiestadisticas)

**Qué se debe crear:**

- Endpoint `GET /api/estadisticas?from=&to=&granularity=DAY`.

#### **Criterios de aceptación:**

- Recibe `from`, `to` y `granularity` (`DAY`, `WEEK` o `MONTH`).
- Consulta `movimiento_inventario`; con `granularity = DAY`, agrupa por fecha y devuelve un mapa `fecha` -> `cantidad de movimientos`.

### **Módulo Transversal — US-26**

#### **US-26 — Documentación y README final**

**Módulo:** Transversal · **Responsable:** Dev 1

**Qué se debe crear:**

- `README.md` en la raíz.
- Documentación en Swagger UI (`/swagger-ui.html`).

#### **Criterios de aceptación:**

- El README incluye descripción general, requisitos previos, guía de arranque, variables de entorno, endpoints principales, instrucciones para correr tests (`mvn test`), diagrama simple de la arquitectura e información de contacto o autor.
- Al levantar la aplicación, Swagger UI está disponible en `http://localhost:8080/swagger-ui.html` con los códigos de error (400, 401, 403, 409, 500) documentados.
