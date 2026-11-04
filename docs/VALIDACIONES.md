# Validaciones y restricciones de datos — Sistema de Gestión de Inventario

Este documento reúne las condiciones que determinan la aceptación de los datos de entrada y la integridad de los
valores: obligatoriedad, longitudes, formatos, tipos, rangos, unicidad y restricciones de representación numérica.

Las validaciones que implementan una regla de negocio se referencian con el identificador `RN` correspondiente; la
regla de negocio se conserva en [REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md). Los contratos de los endpoints y los
códigos HTTP se documentan en [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md) y en
[TABLA_ERRORES.md](TABLA_ERRORES.md).

## 1. Autenticación y usuarios

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-AUTH-01 | `username` (registro) | Obligatorio; entre 3 y 20 caracteres; solo letras, números y `_` (`^[a-zA-Z0-9_]{3,20}$`) | RN113 | `400 VALIDATION_ERROR` |
| VAL-AUTH-02 | `username` (registro) | Único; no puede existir otro usuario con el mismo valor | RN112 | `409 USER_ALREADY_EXISTS` |
| VAL-AUTH-03 | `password` (registro) | Obligatoria; mínimo 8 caracteres; al menos una mayúscula y un número | RN113 | `400 VALIDATION_ERROR` |
| VAL-AUTH-04 | `role` (registro) | Obligatorio; solo letras mayúsculas y `_` (`^[A-Z_]+$`); valores admitidos: `JEFE_TALLER` y `MECANICO` | RN111 | `400 VALIDATION_ERROR`; `400 INVALID_ROLE` si el valor no es un rol admitido |
| VAL-AUTH-05 | `role` (registro) | `ADMIN` no puede asignarse mediante el registro; es un usuario semilla | RN111 | `400 INVALID_ROLE` |
| VAL-AUTH-06 | `username` (login) | Obligatorio; máximo 50 caracteres | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-AUTH-07 | `password` (login) | Obligatoria; no vacía | Contrato de API | `400 VALIDATION_ERROR` |

El `username` se compara por su valor almacenado; la unicidad se garantiza en la validación del servicio y en la
restricción única de la base de datos.

## 2. Categorías

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-CAT-01 | `nombre` | Obligatorio; máximo 100 caracteres | RN102 | `400 VALIDATION_ERROR` |
| VAL-CAT-02 | `nombre` | Único sin distinguir mayúsculas de minúsculas | RN102 | `409 DUPLICATE_RESOURCE` |
| VAL-CAT-03 | `descripcion` | Opcional; máximo 255 caracteres | Contrato de API | `400 VALIDATION_ERROR` |

## 3. Proveedores

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-PROV-01 | `ruc` | Obligatorio; máximo 20 caracteres | RN109 | `400 VALIDATION_ERROR` |
| VAL-PROV-02 | `ruc` | Único | RN109 | `409 DUPLICATE_RESOURCE` |
| VAL-PROV-03 | `razonSocial` | Obligatoria; máximo 150 caracteres | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-PROV-04 | `contacto` | Opcional; máximo 100 caracteres | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-PROV-05 | `telefono` | Opcional; máximo 30 caracteres | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-PROV-06 | `correo` | Opcional; formato de correo electrónico; máximo 150 caracteres | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-PROV-07 | `direccion` | Opcional; máximo 255 caracteres | Contrato de API | `400 VALIDATION_ERROR` |

## 4. Productos

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-PROD-01 | `codigo` | Obligatorio; máximo 50 caracteres; único | RN101 | `400 VALIDATION_ERROR`; `409 DUPLICATE_RESOURCE` |
| VAL-PROD-02 | `codigo` en edición | Único excluyendo el propio producto | RN101 | `409 DUPLICATE_RESOURCE` |
| VAL-PROD-03 | `nombre` | Obligatorio; máximo 150 caracteres | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-PROD-04 | `descripcion` | Opcional; máximo 500 caracteres | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-PROD-05 | `unidadMedida` | Obligatoria; máximo 30 caracteres | RN103 | `400 VALIDATION_ERROR` |
| VAL-PROD-06 | `stockMinimo` | Obligatorio; entero mayor o igual que 0 | RN104 | `400 VALIDATION_ERROR` |
| VAL-PROD-07 | `id_categoria` | Obligatorio; entero mayor que 0; la categoría debe existir y estar activa al crear el producto o al cambiarla | RN102 | `400 VALIDATION_ERROR` |
| VAL-PROD-08 | `id_proveedor` | Obligatorio; entero mayor que 0; el proveedor debe existir y estar activo al crear el producto o al cambiarlo | RN105 | `400 VALIDATION_ERROR` |
| VAL-PROD-09 | `activo` en edición | Opcional; si se omite, conserva el valor actual | RN118 | — |
| VAL-PROD-10 | `nuevoStockMinimo` | Obligatorio; entero mayor o igual que 0 en la actualización específica del stock mínimo | RN104 | `400 VALIDATION_ERROR` |
| VAL-PROD-11 | `stockActual` | No forma parte de los datos de alta o edición; el stock inicial es 0 y solo cambia por movimientos | RN106 | No se acepta como dato editable |
| VAL-PROD-12 | Alias de identificadores | `idCategoria`, `categoriaId`, `idProveedor` y `proveedorId` se aceptan como alias de `id_categoria` e `id_proveedor` | Contrato de API | — |
| VAL-PROD-13 | Edición con historial | Con movimientos registrados, solo se aceptan cambios en `nombre`, `descripcion` y `unidadMedida`; los demás campos deben enviarse sin cambios | RN118 | `400 VALIDATION_ERROR` |

La categoría y el proveedor se validan al crear el producto y cuando la edición propone uno distinto. Si el
producto conserva la referencia actual, no se vuelve a exigir que esta esté activa.

## 5. Entradas de inventario

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-ENT-01 | `productoId` | Obligatorio; el producto debe existir y estar activo | RN202 | `404 NOT_FOUND` |
| VAL-ENT-02 | `proveedorId` | Obligatorio; el proveedor debe existir y estar activo | RN202 | `400 VALIDATION_ERROR` |
| VAL-ENT-03 | `cantidad` | Obligatoria; entero mayor que 0 | RN202 | `400 VALIDATION_ERROR` |
| VAL-ENT-04 | `costoUnitario` | Obligatorio; mayor o igual que 0; hasta 10 dígitos enteros y 2 decimales | RN202 | `400 VALIDATION_ERROR` |
| VAL-ENT-05 | Stock resultante | La suma del stock anterior y la cantidad no puede desbordar el máximo entero admitido | RN211 | `400 VALIDATION_ERROR` |

## 6. Ajustes de inventario

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-AJU-01 | `productoId` | Obligatorio; el producto debe existir y estar activo | RN209 | `404 NOT_FOUND` |
| VAL-AJU-02 | `cantidadFisica` | Obligatoria; entero mayor o igual que 0 | RN209 | `400 VALIDATION_ERROR` |
| VAL-AJU-03 | `justificacion` | Obligatoria; no vacía; máximo 255 caracteres | RN209 | `400 VALIDATION_ERROR` |

## 7. Consumos

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-CON-01 | `finalidad` | Obligatoria; no vacía; máximo 255 caracteres | RN205 | `400 VALIDATION_ERROR` |
| VAL-CON-02 | `items` | Obligatorio; al menos un elemento | RN205 | `400 VALIDATION_ERROR` |
| VAL-CON-03 | `items[].productoId` | Obligatorio; entero mayor que 0; el producto debe existir y estar activo | RN206 | `400 VALIDATION_ERROR` |
| VAL-CON-04 | `items[].cantidad` | Obligatoria; entero mayor que 0 | RN206 | `400 VALIDATION_ERROR` |
| VAL-CON-05 | `referenciaVehiculoCliente` | Opcional; máximo 150 caracteres; si viene en blanco se guarda como nulo | RN205 | `400 VALIDATION_ERROR` |
| VAL-CON-06 | Cantidad total por producto | Si un producto se repite en varios ítems, se suman sus cantidades; la suma no puede desbordar el máximo entero | RN206, RN211 | `400 VALIDATION_ERROR` |
| VAL-CON-07 | Stock disponible | La cantidad total solicitada de cada producto no puede superar el stock actual | RN206 | `409 STOCK_INSUFICIENTE` |

## 8. Consultas de existencias

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-EXI-01 | `ids` | Obligatorio; no puede estar vacío | RN212 | `400 VALIDATION_ERROR` |
| VAL-EXI-02 | `ids` | Los identificadores repetidos se ignoran; se consulta cada producto una sola vez | RN212 | — |
| VAL-EXI-03 | Producto inexistente | Si algún identificador no corresponde a un producto, se rechaza la consulta | RN212 | `404 NOT_FOUND` |
| VAL-EXI-04 | Producto inactivo | Si algún producto existe pero está inactivo, se rechaza la consulta | RN212 | `400 VALIDATION_ERROR` |

## 9. Reportes y estadísticas

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-REP-01 | `desde`, `hasta` | Obligatorios en los reportes de movimientos y consumo; formato `yyyy-MM-dd`; `hasta` no puede ser anterior a `desde` | RN405 | `400 VALIDATION_ERROR` |
| VAL-REP-02 | Rango de fechas | Ambos extremos son inclusivos: se incluye todo el día `desde` y todo el día `hasta` | RN405 | — |
| VAL-REP-03 | `tipo` | Opcional; si se informa, debe ser `ENTRADA`, `CONSUMO` o `AJUSTE` | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-REP-04 | `page` | Entero; los valores negativos se normalizan a 0 | RN414 | `400 VALIDATION_ERROR` si el formato es inválido |
| VAL-REP-05 | `size` | Entero; se limita al rango 1–100; valor por defecto 10 | RN414 | `400 VALIDATION_ERROR` si el formato es inválido |
| VAL-REP-06 | `from`, `to` | Obligatorios en estadísticas; formato `yyyy-MM-dd`; `to` no puede ser anterior a `from` | RN405 | `400 VALIDATION_ERROR` |
| VAL-REP-07 | `granularity` | Obligatorio en estadísticas; valores admitidos: `DAY`, `WEEK` o `MONTH` | RN405 | `400 VALIDATION_ERROR` |
| VAL-REP-08 | Paginación del reporte de movimientos | Solo la lista de movimientos se pagina; los totales por tipo cubren todo el filtro y no cambian al cambiar de página | RN414 | — |
| VAL-REP-09 | Reportes no paginados | Los reportes de consumo, valor de inventario y bajo stock, y todas las exportaciones, devuelven el resultado completo | RN414 | — |

## 10. Exportaciones

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-EXP-01 | `formato` | Obligatorio; valores admitidos: `pdf`, `excel` o `xlsx`; no distingue mayúsculas | RN412 | `400 VALIDATION_ERROR` |
| VAL-EXP-02 | Fechas de exportación | Las exportaciones de movimientos y consumo heredan las reglas de `desde` y `hasta` del reporte de origen | RN405, RN412 | `400 VALIDATION_ERROR` |
| VAL-EXP-03 | Paginación en exportaciones | Las exportaciones ignoran `page` y `size` y exportan el contenido completo | RN414 | — |

## 11. Restricciones generales de representación

| ID | Elemento | Condición | Origen | Si incumple |
|----|----------|-----------|--------|-------------|
| VAL-GEN-01 | Stock | `stockActual`, `stockMinimo`, cantidades y variaciones se representan como enteros; las operaciones que pueden desbordar el máximo entero se rechazan | RN211 | `400 VALIDATION_ERROR` |
| VAL-GEN-02 | Costos y valores monetarios | Se representan con hasta 12 dígitos de precisión y 2 decimales; el costo unitario de entrada admite hasta 10 enteros y 2 decimales | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-GEN-03 | Fechas | `LocalDate` en formato `yyyy-MM-dd`; los instantes se almacenan y responden en UTC con formato `yyyy-MM-dd'T'HH:mm:ss'Z'` | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-GEN-04 | JSON de entrada | El cuerpo debe ser JSON válido y legible; un JSON malformado se rechaza | Contrato de API | `400 VALIDATION_ERROR` |
| VAL-GEN-05 | Parámetros de URL y path | Los valores deben tener el tipo esperado (número, fecha, enumeración) | Contrato de API | `400 VALIDATION_ERROR` |

## 12. Verificación

Las restricciones de este documento se contrastaron estáticamente con los DTO de entrada, las anotaciones de
validación, los servicios y las consultas del código actual. No se ejecutaron compilaciones ni pruebas de runtime;
por tanto, la columna «Si incumple» refleja el contrato documentado y el manejo implementado, no una comprobación
dinámica.
