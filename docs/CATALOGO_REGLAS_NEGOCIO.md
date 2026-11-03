# Catálogo de reglas de negocio — Sistema de Gestión de Inventario (Taller Mecánico)

Documento generado a partir de `PLAN.md` (numeración RN original) y del código actual. Cada regla indica dónde se
aplica y qué error produce cuando se incumple. Para los códigos de error ver [TABLA_ERRORES.md](TABLA_ERRORES.md) y
para los contratos [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md), que prevalece si hay diferencias.

Las reglas RN401 a RN403 y RN406 no existen en la numeración original.

## 1. Seguridad (SEC)

| Regla | Descripción | Implementación | Error al incumplir |
|-------|-------------|----------------|--------------------|
| RN107 | Solo `JEFE_TALLER` administra productos, categorías y proveedores. `MECANICO` no puede modificar los catálogos | `SecurityConfig` (`POST /api/categorias`, `POST /api/proveedores`, `POST/PUT/PATCH /api/productos/**` con `hasRole("JEFE_TALLER")`) | 403 `FORBIDDEN` |
| RN109 | El RUC del proveedor es único | `ProveedorServiceImpl.crear` (`existsByRuc`) | 409 `DUPLICATE_RESOURCE` |
| SEC-01 | Solo `ADMIN` puede registrar usuarios | `SecurityConfig` (`POST /api/auth/register`) | 403 `FORBIDDEN` |
| SEC-02 | El rol es obligatorio y solo puede ser `JEFE_TALLER` o `MECANICO`; `ADMIN` es usuario seed y no se asigna por registro | `AuthServiceImpl.resolveRole`, `AdminSeeder` | 400 `INVALID_ROLE` |
| SEC-03 | El username es único | `AuthServiceImpl.register` | 409 `USER_ALREADY_EXISTS` |
| SEC-04 | Contraseña de mínimo 8 caracteres, con al menos una mayúscula y un número; username de 3 a 20 caracteres (letras, números y `_`) | `RegisterRequest` (Bean Validation) | 400 `VALIDATION_ERROR` |
| SEC-05 | JWT HS256 con claims `sub`, `username` y `role`. El rol efectivo se toma de la base de datos, no del claim | `JwtUtil`, `JwtAuthFilter` | 401 `TOKEN_INVALID` / `TOKEN_EXPIRED` |
| SEC-06 | Token inválido, expirado, de usuario inexistente o inactivo bloquea los endpoints no públicos | `JwtAuthFilter`, `TokenAuthenticationEntryPoint` | 401 `TOKEN_INVALID`, `TOKEN_EXPIRED`, `ACCOUNT_DISABLED`; sin token, 401 `UNAUTHORIZED` |
| SEC-07 | Endpoints públicos: `/api/auth/login`, `/actuator/health`, `/actuator/info` y `/error`. El resto exige autenticación | `SecurityConfig` (`/actuator/info` pendiente, ver sección 6) | 401 |
| SEC-08 | Fail-Fast: la aplicación no arranca si `jwt.secret` está vacío o tiene menos de 32 bytes, o si `jwt.expiration <= 0` | `AppConfig` | `IllegalStateException` ("FATAL ...") al arrancar |
| SEC-09 | `ADMIN` solo registra usuarios: no accede a los endpoints de negocio (catálogos, productos, inventario, alertas, dashboard, reportes ni estadísticas) | Aplicado en escrituras (`SecurityConfig`, `@PreAuthorize`); pendiente en consultas, ver sección 6 | 403 `FORBIDDEN` |

## 2. Gestión de productos (M100)

| Regla | Descripción | Implementación | Error al incumplir |
|-------|-------------|----------------|--------------------|
| RN101 | El `codigo` del producto es único, validado antes de guardar (también al editar, excluyendo el propio producto) | `ProductoServiceImpl.crear` / `aplicarCambios` | 409 `DUPLICATE_RESOURCE` |
| RN102 | `nombre` y categoría son obligatorios. La categoría debe existir y estar activa. El nombre de categoría es único sin distinguir mayúsculas | `CreateProductoRequest`, `ProductoServiceImpl.obtenerCategoriaActiva`, `CategoriaServiceImpl.crear` | 400 `VALIDATION_ERROR`; 409 `DUPLICATE_RESOURCE` (categoría duplicada) |
| RN103 | `unidadMedida` es obligatoria (máximo 30 caracteres) | `CreateProductoRequest`, `UpdateProductoRequest` | 400 `VALIDATION_ERROR` |
| RN104 | `stockMinimo` es un entero `>= 0` | `CreateProductoRequest`, `StockMinimoRequest`, `ProductoServiceImpl.actualizarStockMinimo` | 400 `VALIDATION_ERROR` |
| RN105 | Todo producto tiene un proveedor principal existente y activo | `ProductoServiceImpl.obtenerProveedorActivo` | 400 `VALIDATION_ERROR` |
| RN106 | El stock inicial es 0 y el stock no se edita desde el formulario de producto (solo por movimientos). El SKU se genera como `CP-` más 6 dígitos del id | `ProductoServiceImpl.crear` (`setStockActual(0)`, `generarSku`); los DTOs de producto no incluyen `stockActual` | — |
| RN108 | Los productos con historial no se eliminan físicamente. Solo desactivación lógica (`activo = false`), que puede aplicarse siempre, incluso con movimientos | `ProductoServiceImpl.desactivar` (no existe `DELETE`) | — |
| M100-01 | Con movimientos registrados solo se pueden cambiar `nombre`, `descripcion` y `unidadMedida`. Código, categoría, proveedor, `stockMinimo` y `activo` deben enviarse sin cambios | `ProductoServiceImpl.validarEdicionConHistorial` | 400 `VALIDATION_ERROR` |
| M100-02 | Cambiar el stock mínimo mediante `PUT /api/productos/{id}/stock-minimo` no comprueba si hay movimientos | `ProductoServiceImpl.actualizarStockMinimo` | — |
| M100-03 | El listado pagina con `page >= 0` y `size` limitado a 1–100, ordenado por `nombre` y `id`, filtrable por nombre (parcial, sin distinguir mayúsculas) y categoría. Incluye productos inactivos | `ProductoServiceImpl.listar` | — |
| M100-04 | `stockCritico` es `stockActual <= stockMinimo` | `ProductoMapper`, `ExistenciaResponse` | — |
| M100-05 | `GET /api/productos` solo lo consultan `JEFE_TALLER` y `MECANICO`; `ADMIN` no | Pendiente, ver sección 6 | 403 `FORBIDDEN` |

## 3. Gestión de inventario (M200)

| Regla | Descripción | Implementación | Error al incumplir |
|-------|-------------|----------------|--------------------|
| RN201 | Solo `JEFE_TALLER` registra entradas con producto, proveedor, cantidad y costo unitario. Fecha y usuario los asigna el sistema (reloj del servidor y token) | `MovimientoController.entrada` (`@PreAuthorize`), `MovimientoServiceImpl.entrada` | 403 `FORBIDDEN` |
| RN202 | Cantidad `> 0`, costo unitario `>= 0` (hasta 10 enteros y 2 decimales), producto y proveedor activos | `EntradaRequest`, `MovimientoServiceImpl.entrada` | 400 `VALIDATION_ERROR`; 404 `NOT_FOUND` (producto inexistente o inactivo) |
| RN203 | La entrada incrementa `stockActual` y sobrescribe `costoUltimaCompra` con el costo de esa entrada | `MovimientoServiceImpl.entrada` | — |
| RN204 | El mecánico registra los productos usados directamente, sin solicitud al jefe. `JEFE_TALLER` también puede registrar consumos | `ConsumoController` (`hasAnyRole('MECANICO','JEFE_TALLER')`) | 403 `FORBIDDEN` |
| RN205 | Cada consumo identifica al usuario autenticado, fecha, productos, cantidades y finalidad. La referencia a vehículo/cliente es opcional (máximo 150 caracteres); la finalidad es obligatoria (máximo 255) | `ConsumoRequest`, `ConsumoServiceImpl.registrar` | 400 `VALIDATION_ERROR` |
| RN206 | Cada producto consumido debe existir, estar activo y tener stock suficiente. Cantidad `> 0`. El stock nunca queda negativo | `ConsumoStockServiceImpl.validarDisponibilidad`, `Producto.descontarStock` | 400 `VALIDATION_ERROR` (producto no disponible o cantidad inválida); 409 `STOCK_INSUFICIENTE` |
| RN207 | El consumo es atómico: si falla un ítem no se descuenta nada. La validación previa agrupa cantidades por producto cuando se repite en varios ítems, y el descuento usa bloqueo de fila | `ConsumoServiceImpl` y `ConsumoStockServiceImpl` (`@Transactional`, `findByIdAndActivoTrueForUpdate`) | 409 `STOCK_INSUFICIENTE` |
| RN208 | Cada movimiento conserva tipo, producto, cantidad/variación, stock anterior y posterior, fecha, usuario y motivo | `MovimientoInventario` | — |
| RN209 | Solo `JEFE_TALLER` ajusta. `cantidadFisica >= 0`, justificación obligatoria (máximo 255). `variacion = cantidadFisica - stockAnterior`. El ajuste no modifica `costoUltimaCompra` | `MovimientoController.ajuste`, `MovimientoServiceImpl.ajuste` | 403 `FORBIDDEN`; 400 `VALIDATION_ERROR`; 404 `NOT_FOUND` |
| M200-01 | Valores de `cantidad` y `variacion` por tipo: `ENTRADA` (`cantidad` = `variacion` = unidades ingresadas), `CONSUMO` (`cantidad` = unidades descontadas, `variacion` nulo), `AJUSTE` (`cantidad` nula, `variacion` con signo) | `MovimientoServiceImpl`, `ConsumoStockServiceImpl` | — |
| M200-02 | Una entrada que desborda el máximo entero de stock se rechaza. Lo mismo aplica a la suma de cantidades repetidas en un consumo | `MovimientoServiceImpl.entrada`, `ConsumoStockServiceImpl.agruparCantidades` | 400 `VALIDATION_ERROR` |
| M200-03 | `GET /api/existencias` exige `ids`, ignora ids repetidos, devuelve 404 si algún producto no existe y 400 si está inactivo | `ExistenciaServiceImpl.consultar` | 400 / 404 |
| M200-04 | `GET /api/existencias` solo lo consultan `JEFE_TALLER` y `MECANICO`; `ADMIN` no | Pendiente, ver sección 6 | 403 `FORBIDDEN` |

## 4. Control y abastecimiento (M300)

| Regla | Descripción | Implementación | Error al incumplir |
|-------|-------------|----------------|--------------------|
| RN301 | Todo producto tiene un stock mínimo configurado (obligatorio al crear) | `CreateProductoRequest` | 400 `VALIDATION_ERROR` |
| RN302 | Hay bajo stock cuando `stockActual <= stockMinimo`. Solo cuentan productos activos | `AlertaServiceImpl.tieneBajoStock` | — |
| RN303 | La alerta incluye código, nombre, stock actual, stock mínimo, cantidad sugerida y proveedor principal (razón social) | `AlertaStock`, `AlertaServiceImpl.toAlerta` | — |
| RN304 | Las alertas se calculan al consultar, no se almacenan, por lo que reflejan al instante cualquier cambio de stock o de mínimo | `AlertaServiceImpl.generarAlertas` | — |
| RN305 | Cantidad sugerida = `stockMinimo - stockActual` (referencial). Las alertas se ordenan por cantidad sugerida descendente y luego por nombre | `AlertaServiceImpl` | — |
| M300-01 | Una tarea programada revisa el stock según la expresión `ALERTAS_CRON`, en zona America/Lima, y escribe el resultado en el log. Con lista vacía registra el mensaje de "sin alertas" | `AlertaScheduler.revisarStock` | — |
| M300-02 | `GET /api/alertas` solo lo consultan `JEFE_TALLER` y `MECANICO`; `ADMIN` no | Pendiente, ver sección 6 | 403 `FORBIDDEN` |

## 5. Consultas y reportes (M400)

| Regla | Descripción | Implementación | Error al incumplir |
|-------|-------------|----------------|--------------------|
| RN404 | El reporte de consumo identifica producto, cantidad utilizada, fecha, mecánico y finalidad. Agrupa por producto y mecánico y ordena por nombre de producto y mecánico | `ReporteConsumoServiceImpl` | 400 `VALIDATION_ERROR` |
| RN405 | Los filtros por fecha exigen ambos extremos y que la fecha inicial no sea posterior a la final. Ambos extremos son inclusivos. Las estadísticas exigen además `granularity` (`DAY`, `WEEK` o `MONTH`) | `ReporteMovimientosServiceImpl`, `ReporteConsumoServiceImpl`, `EstadisticasServiceImpl` | 400 `VALIDATION_ERROR` |
| RN407 | Valor estimado = suma de `stockActual * costoUltimaCompra` de los productos activos. Es una estimación operativa, no contable | `ValorInventarioCalculator.calcular` | — |
| RN408 | Los reportes son de solo lectura sobre movimientos confirmados. Un producto sin costo de última compra se identifica, se excluye del total y se informa en `productosSinCosto` | `ValorInventarioCalculator`, servicios con `@Transactional(readOnly = true)` | — |
| M400-01 | Los rangos de fechas, cortes de día, semana (lunes) y mes (día 1) se calculan en zona America/Lima; el almacenamiento y las respuestas usan UTC | `ClockConfig.ZONA_NEGOCIO`, `EstadisticasBucketing` | — |
| M400-02 | Las estadísticas omiten los períodos sin movimientos; las claves son la fecha de inicio del período | `EstadisticasBucketing.agrupar` | — |
| M400-03 | El reporte de bajo stock agrupa por proveedor en orden alfabético sin distinguir mayúsculas | `BajoStockAgrupador` | — |
| M400-04 | La exportación exige `formato` (`pdf`, `excel` o `xlsx`) y entrega el mismo contenido del reporte en archivo adjunto | `FormatoReporte.from`, `ReporteExportController` | 400 `VALIDATION_ERROR` |
| M400-05 | El dashboard muestra totales de inventario activo, KPIs del mes en curso (desde el día 1), las 5 alertas más urgentes y los 5 movimientos más recientes. Solo `JEFE_TALLER`; `ADMIN` y `MECANICO` no acceden | `DashboardServiceImpl`; `SecurityConfig` todavía permite a `MECANICO`, ver sección 6 | 403 `FORBIDDEN` (roles `ADMIN` y `MECANICO`) |
| M400-06 | Los reportes (`movimientos`, `consumo`, `valor-inventario`, `bajo-stock`), sus exportaciones y `GET /api/estadisticas` solo los consulta `JEFE_TALLER`; `ADMIN` y `MECANICO` no | Pendiente, ver sección 6 | 403 `FORBIDDEN` |
| M400-07 | `GET /api/reportes/movimientos` pagina con `page >= 0` y `size` limitado a 1–100 (por defecto 10), ordenado por `fecha` e `id` descendentes. Los totales por tipo cubren todo el filtro, no solo la página. Una página fuera de rango devuelve la lista vacía. Los demás reportes y todas las exportaciones no se paginan | Pendiente, ver sección 6 | 400 `VALIDATION_ERROR` (`page` o `size` con formato inválido) |

## 6. Observaciones sobre el estado actual

- `CATALOGO_ENDPOINTS.md` es la referencia vigente para el control de acceso por rol y para la paginación. El código aún
  no cumple estas reglas (las tareas están en `TASK.md`):
    - SEC-09, M100-05, M200-04, M300-02 y M400-06: `GET /api/productos`, `/api/existencias`, `/api/alertas`, los reportes,
      las exportaciones y `/api/estadisticas` no tienen restricción de rol; hoy los puede consultar cualquier usuario
      autenticado, incluido `ADMIN`.
    - M400-05: `SecurityConfig` permite a `MECANICO` consultar `/api/dashboard`.
    - M400-07: `GET /api/reportes/movimientos` devuelve la lista completa, sin paginar.
    - SEC-07: `/actuator/info` no está en `permitAll`.
- Las reglas de las secciones 1 a 5 con códigos `SEC-`, `M100-`, `M200-`, `M300-` y `M400-` no figuran en la numeración
  original de `PLAN.md`: se añadieron para registrar comportamiento implementado que no tenía número de RN.
