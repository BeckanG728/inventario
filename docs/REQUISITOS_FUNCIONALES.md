# Requisitos funcionales — Sistema de Gestión de Inventario

Este documento describe las funcionalidades y comportamientos observables que no corresponden exclusivamente a reglas
de negocio ni a contratos de API: alertas, revisión programada, reportes, dashboard, estadísticas, exportaciones y
consultas.

Cuando una funcionalidad contiene una política de negocio, la política se conserva en
[REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md) y aquí se describe únicamente la funcionalidad que la expone. Los
endpoints, parámetros, estructuras de respuesta y códigos HTTP se documentan en
[CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md).

## 1. Alertas de bajo stock (RF-01)

**Origen:** RN302, RN303, RN304, RN305.

- El sistema lista los productos activos que se encuentran en condición de bajo stock.
- Cada alerta incluye, como mínimo: código del producto, nombre, stock actual, stock mínimo, cantidad sugerida y
  proveedor principal (razón social).
- La cantidad sugerida se calcula como `stockMinimo - stockActual`; es referencial (política en `RN305`).
- Las alertas se ordenan por cantidad sugerida de forma descendente y, ante empate, por nombre.
- Las alertas se calculan al consultar y no se almacenan; por ello reflejan de inmediato cualquier cambio de stock o
  de stock mínimo.
- La consulta está disponible para `JEFE_TALLER` y `MECANICO`.

## 2. Revisión programada de stock (RF-02)

**Origen:** RN306.

- Una tarea programada revisa periódicamente el stock según la expresión cron configurada en `ALERTAS_CRON`.
- La tarea se ejecuta en la zona `America/Lima`.
- Escribe en el log un resumen de la revisión y el detalle de cada alerta.
- Si no hay alertas, registra el mensaje de «sin alertas».
- La tarea no modifica stock ni persiste alertas; su resultado es informativo.

La configuración técnica de la tarea se detalla en [CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md).

## 3. Reporte de movimientos (RF-03)

**Origen:** RN405, RN414.

- Presenta los movimientos de inventario de un rango de fechas, con filtro opcional por tipo.
- Informa totales por tipo (`ENTRADA`, `CONSUMO`, `AJUSTE`) calculados sobre todo el filtro, no solo sobre la página.
- La lista de movimientos está paginada; el detalle del reporte de valor de inventario también se pagina, mientras que las exportaciones entregan el contenido completo.
- Ordena por fecha descendente y luego por identificador descendente.
- Una página fuera de rango devuelve la lista de movimientos vacía, conservando los totales.

El contrato detallado se encuentra en [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md).

## 4. Reporte de valor de inventario (RF-04)

**Origen:** RN407, RN408.

- Calcula el valor estimado del inventario activo como la suma de `stockActual * costoUltimaCompra` por producto.
- Presenta el total general, la cantidad de productos con costo y la cantidad de productos sin costo.
- Incluye el detalle por producto.
- Los productos sin costo de última compra se identifican con costo nulo y valor cero, y se excluyen del total.
- Informa cuántos productos quedaron excluidos.
- Es una estimación operativa, no un valor contable oficial.

## 5. Dashboard (RF-05)

**Origen:** RN413.

- Muestra un resumen del inventario activo: total de productos, productos con stock normal, productos con bajo stock
  y productos sin stock.
- Muestra los KPI del mes en curso, calculados desde el día 1: valor estimado del inventario, productos sin costo,
  entradas, consumos y ajustes.
- Muestra las 5 alertas más urgentes.
- Muestra los 5 movimientos más recientes.
- El acceso corresponde a `JEFE_TALLER`.

## 6. Estadísticas (RF-06)

**Origen:** RN410.

- Cuenta los movimientos de un rango de fechas y los agrupa según la granularidad indicada: `DAY`, `WEEK` o `MONTH`.
- Omite los períodos sin movimientos.
- Las claves del resultado son la fecha de inicio del período: el día para `DAY`, el lunes de la semana para `WEEK`
  y el día 1 del mes para `MONTH`.
- Los cortes se calculan en la zona `America/Lima`.
- El endpoint `/api/estadisticas/por-tipo` informa además las unidades y los costos de entrada, consumo y ajuste por
  período, con la opción de filtrar por producto; en ese caso los períodos sin movimientos se muestran en cero.
- Los costos se calculan desde la tabla de movimientos como `cantidad × costoUnitario` registrado en cada operación;
  las entradas usan el costo real de compra y las salidas el snapshot de `costoUltimaCompra` al consumir (RN208).
  Los ajustes no se valoran en este reporte.
- El acceso corresponde a `JEFE_TALLER`.

## 7. Exportaciones (RF-07)

**Origen:** RN412, RN414.

- Permite exportar el reporte de valor de inventario en formato PDF o Excel. El reporte de movimientos no se exporta.
- El archivo entregado contiene el mismo contenido del reporte de origen.
- La exportación se entrega como archivo adjunto con el nombre y la extensión correspondientes al formato.
- Las exportaciones no se paginan: exportan el contenido completo e ignoran `page` y `size`.
- Las reglas de fechas y de formato se detallan en [VALIDACIONES.md](VALIDACIONES.md).

## 8. Consultas de productos y existencias (RF-08)

**Origen:** RN119, RN212.

- Permite listar productos con paginación, filtro parcial por nombre y filtro por categoría.
- El listado incluye productos activos e inactivos.
- Permite consultar el stock de uno o varios productos por identificador.
- La consulta de existencias exige al menos un identificador, ignora identificadores repetidos y rechaza productos
  inexistentes o inactivos según el contrato.
- El detalle de parámetros y respuestas está en [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md).

## 9. Gestión de usuarios (RF-09)

**Origen:** RN110, RN111, RN112, RN113.

- El usuario `ADMIN` puede registrar usuarios con rol `JEFE_TALLER` o `MECANICO`.
- El sistema valida el formato del username, la política de contraseña, la unicidad del username y la validez del rol.
- El rol `ADMIN` no se asigna mediante el registro.

Las políticas asociadas se documentan en [POLITICAS_DE_SEGURIDAD.md](POLITICAS_DE_SEGURIDAD.md) y las restricciones de
datos en [VALIDACIONES.md](VALIDACIONES.md).

## 10. Unidades base, presentaciones y menudeo (RF-10)

**Origen:** RN501 a RN507, RN103, RN118.

- El catálogo de unidades (`UND`, `CAJA`, `PAR`, `JUEGO`, `KIT`, `LITRO`, `GALON`, `KG`, `METRO`, `CM`, `ROLLO`, `LATA`)
  es dato de negocio sembrado en la migración V9; no depende del perfil ni del seeder demo.
- Cada producto tiene una unidad base estable e inmutable desde su primer movimiento. Las existencias y los movimientos
  se expresan en esa unidad. Los envases de consumo único (botella de 1 L, envase de 250 g, lata) se controlan como
  `UND`; la longitud se controla en `CM`.
- Cada categoría tiene un conjunto de unidades válidas. El alta de categoría exige al menos una; el frontend solo
  ofrece esas unidades al crear o editar un producto.
- Un producto puede tener una presentación comercial (`CAJA`, `METRO`, `ROLLO`) con un factor entero. Una entrada de
  3 cajas de 20 unidades registra 60 unidades base y divide el costo por el factor.
- Solo se admite cantidad decimal en longitud (1,5 m = 150 cm). No se permite "media caja".
- La apertura de un envase no genera un movimiento adicional: la conversión ya ocurrió al registrar la operación.
  Cada movimiento conserva como snapshot la unidad introducida, el factor aplicado y la cantidad en unidad base.
- El endpoint `GET /api/unidades` expone el catálogo; `GET /api/categorias/{id}/unidades` y
  `PUT /api/categorias/{id}/unidades` gestionan la asociación por categoría.

## 11. Verificación

Los requisitos se contrastaron estáticamente con los servicios de alertas, reportes, dashboard, estadísticas,
exportaciones, el catálogo de unidades, la conversión de presentaciones y los controladores correspondientes.
No se ejecutaron pruebas de runtime.
