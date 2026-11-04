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

## 3. Reporte de consumo (RF-03)

**Origen:** RN404.

- El reporte permite identificar, para cada consumo, el producto, la cantidad utilizada, la fecha, el mecánico y la
  finalidad declarada.
- Agrupa los consumos por producto y mecánico.
- Ordena por nombre de producto y luego por mecánico, sin distinguir mayúsculas.
- Para cada grupo informa las finalidades distintas y las fechas de los movimientos agrupados.
- Los detalles del endpoint y del rango de fechas están en [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md).

## 4. Reporte de movimientos (RF-04)

**Origen:** RN405, RN414.

- Presenta los movimientos de inventario de un rango de fechas, con filtro opcional por tipo.
- Informa totales por tipo (`ENTRADA`, `CONSUMO`, `AJUSTE`) calculados sobre todo el filtro, no solo sobre la página.
- La lista de movimientos está paginada; los demás reportes y las exportaciones no se paginan.
- Ordena por fecha descendente y luego por identificador descendente.
- Una página fuera de rango devuelve la lista de movimientos vacía, conservando los totales.

El contrato detallado se encuentra en [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md).

## 5. Reporte de valor de inventario (RF-05)

**Origen:** RN407, RN408.

- Calcula el valor estimado del inventario activo como la suma de `stockActual * costoUltimaCompra` por producto.
- Presenta el total general, la cantidad de productos con costo y la cantidad de productos sin costo.
- Incluye el detalle por producto.
- Los productos sin costo de última compra se identifican con costo nulo y valor cero, y se excluyen del total.
- Informa cuántos productos quedaron excluidos.
- Es una estimación operativa, no un valor contable oficial.

## 6. Reporte de bajo stock (RF-06)

**Origen:** RN411.

- Lista los productos en condición de bajo stock agrupados por proveedor.
- Los grupos de proveedor se ordenan alfabéticamente sin distinguir mayúsculas.
- Cada grupo incluye los productos con código, nombre, stock actual, stock mínimo y cantidad sugerida.

## 7. Dashboard (RF-07)

**Origen:** RN413.

- Muestra un resumen del inventario activo: total de productos, productos con stock normal, productos con bajo stock
  y productos sin stock.
- Muestra los KPI del mes en curso, calculados desde el día 1: valor estimado del inventario, productos sin costo,
  entradas, consumos y ajustes.
- Muestra las 5 alertas más urgentes.
- Muestra los 5 movimientos más recientes.
- El acceso corresponde a `JEFE_TALLER`.

## 8. Estadísticas (RF-08)

**Origen:** RN410.

- Cuenta los movimientos de un rango de fechas y los agrupa según la granularidad indicada: `DAY`, `WEEK` o `MONTH`.
- Omite los períodos sin movimientos.
- Las claves del resultado son la fecha de inicio del período: el día para `DAY`, el lunes de la semana para `WEEK`
  y el día 1 del mes para `MONTH`.
- Los cortes se calculan en la zona `America/Lima`.
- El acceso corresponde a `JEFE_TALLER`.

## 9. Exportaciones (RF-09)

**Origen:** RN412, RN414.

- Permite exportar los reportes de movimientos, consumo y bajo stock en formato PDF o Excel.
- El archivo entregado contiene el mismo contenido del reporte de origen.
- La exportación se entrega como archivo adjunto con el nombre y la extensión correspondientes al formato.
- Las exportaciones no se paginan: exportan el contenido completo e ignoran `page` y `size`.
- Las reglas de fechas y de formato se detallan en [VALIDACIONES.md](VALIDACIONES.md).

## 10. Consultas de productos y existencias (RF-10)

**Origen:** RN119, RN212.

- Permite listar productos con paginación, filtro parcial por nombre y filtro por categoría.
- El listado incluye productos activos e inactivos.
- Permite consultar el stock de uno o varios productos por identificador.
- La consulta de existencias exige al menos un identificador, ignora identificadores repetidos y rechaza productos
  inexistentes o inactivos según el contrato.
- El detalle de parámetros y respuestas está en [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md).

## 11. Gestión de usuarios (RF-11)

**Origen:** RN110, RN111, RN112, RN113.

- El usuario `ADMIN` puede registrar usuarios con rol `JEFE_TALLER` o `MECANICO`.
- El sistema valida el formato del username, la política de contraseña, la unicidad del username y la validez del rol.
- El rol `ADMIN` no se asigna mediante el registro.

Las políticas asociadas se documentan en [POLITICAS_DE_SEGURIDAD.md](POLITICAS_DE_SEGURIDAD.md) y las restricciones de
datos en [VALIDACIONES.md](VALIDACIONES.md).

## 12. Verificación

Los requisitos se contrastaron estáticamente con los servicios de alertas, reportes, dashboard, estadísticas,
exportaciones y los controladores correspondientes. No se ejecutaron pruebas de runtime.
