# Reglas de negocio — Sistema de Gestión de Inventario (Taller Mecánico)

Este documento contiene **exclusivamente reglas de negocio**: políticas, restricciones y condiciones propias del
dominio del taller mecánico. No incluye validaciones de entrada, políticas de seguridad, decisiones técnicas ni
contratos de API; esos contenidos se documentan en los documentos enlazados al final.

Los identificadores `RN101` a `RN109`, `RN201` a `RN209`, `RN301` a `RN305`, `RN404`, `RN405`, `RN407` y `RN408` se
conservan de `PLAN.md`. `RN118` se asignó en la reclasificación a las antiguas reglas `M100-01`
y `M100-02`, que no tenían número `RN`. Las reglas que se dividieron mantienen su identificador para la parte de negocio
y remiten a los documentos especializados para las partes trasladadas.

Las reglas RN401 a RN403 y RN406 no existen en la numeración original.

## 1. Seguridad operativa

### RN107 — Administración exclusiva del catálogo

- **Enunciado:** La administración del catálogo de productos, categorías y proveedores corresponde únicamente al
  Jefe de Taller. El mecánico puede consultar la información que necesite para su trabajo, pero no puede modificar
  el catálogo.
- **Condiciones:** aplica a la creación, edición, cambio de stock mínimo y desactivación de productos, así como a la
  creación de categorías y proveedores.
- **Consecuencia:** la operación de modificación es rechazada y el catálogo permanece sin cambios.
- **Trazabilidad:** la política de acceso que la hace cumplir se documenta en
  [POLITICAS_DE_SEGURIDAD.md](POLITICAS_DE_SEGURIDAD.md).

## 2. Gestión de productos

### RN102 — Categoría activa del producto

- **Enunciado:** Todo producto debe estar asociado a una categoría existente y activa. No se admite crear un
  producto con una categoría inexistente o inactiva, ni cambiarlo a una categoría en esas condiciones.
- **Condiciones:** la categoría es obligatoria en el alta y en la actualización del producto; si el producto
  conserva su categoría actual, no se exige que esta vuelva a validarse.
- **Consecuencia:** se rechaza el alta o la actualización y no se guarda el producto.
- **Trazabilidad:** las restricciones de obligatoriedad y unicidad asociadas se detallan en
  [VALIDACIONES.md](VALIDACIONES.md).

### RN105 — Proveedor principal activo

- **Enunciado:** Todo producto debe tener un proveedor principal existente y activo.
- **Condiciones:** el proveedor es obligatorio en el alta y en la actualización del producto; si el producto
  conserva su proveedor actual, no se exige que este vuelva a validarse.
- **Consecuencia:** se rechaza el alta o la actualización y no se guarda el producto.
- **Trazabilidad:** las restricciones de identificación del proveedor se detallan en
  [VALIDACIONES.md](VALIDACIONES.md).

### RN106 — Origen y conservación del stock

- **Enunciado:** El stock de un producto nuevo comienza en cero. El stock no se modifica desde el formulario de
  producto; solo cambia por movimientos de entrada, consumo o ajuste registrados en el sistema.
- **Condiciones:** la creación del producto fija el stock inicial en cero; las actualizaciones de datos maestros no
  aceptan un valor de stock.
- **Consecuencia:** cualquier intento de fijar el stock desde la edición del producto no surte efecto; el stock
  solo puede variar mediante movimientos.
- **Trazabilidad:** la generación del SKU a partir del identificador es una decisión técnica y se documenta en
  [CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md).

### RN108 — Conservación del historial de productos

- **Enunciado:** Los productos con movimientos históricos no se eliminan físicamente. Solo se desactivan
  lógicamente, conservando su historial.
- **Condiciones:** la desactivación lógica puede aplicarse siempre, incluso cuando el producto ya tiene movimientos
  registrados.
- **Consecuencia:** el producto deja de estar activo, pero sus datos y movimientos permanecen disponibles para
  consulta y trazabilidad.

### RN118 — Restricciones de edición con historial

- **Enunciado:** Cuando un producto ya tiene movimientos registrados, solo se pueden modificar su nombre, su
  descripción y su unidad de medida. El código, la categoría, el proveedor, el stock mínimo y el estado activo deben
  permanecer sin cambios.
- **Condiciones:** la edición completa del producto debe enviar esos campos con sus valores actuales. La
  actualización específica del stock mínimo es una excepción: se realiza directamente y no comprueba el historial de
  movimientos.
- **Consecuencia:** se rechaza la edición completa si se intenta cambiar un campo no permitido; la actualización
  específica del stock mínimo sí se aplica.

## 3. Gestión de inventario

### RN201 — Registro de entradas

- **Enunciado:** Solo el Jefe de Taller registra entradas de stock, indicando producto, proveedor, cantidad y costo
  unitario. La fecha y el usuario responsables los asigna el sistema a partir del momento del registro y del usuario
  autenticado; no son datos que el operador pueda fijar.
- **Condiciones:** cada entrada representa unidades que ingresan efectivamente al inventario.
- **Consecuencia:** la entrada no autorizada se rechaza y no altera el stock ni el costo de última compra.

### RN202 — Condiciones de una entrada

- **Enunciado:** Una entrada solo puede registrarse para un producto y un proveedor activos, con una cantidad
  efectivamente ingresada mayor que cero y un costo unitario no negativo.
- **Condiciones:** el producto y el proveedor deben existir y estar activos; la cantidad y el costo deben respetar
  los límites numéricos definidos para la operación.
- **Consecuencia:** se rechaza la entrada y no se modifica el inventario.
- **Trazabilidad:** los rangos, formatos y límites numéricos se detallan en [VALIDACIONES.md](VALIDACIONES.md).

### RN203 — Efecto de la entrada

- **Enunciado:** Al confirmar una entrada, el stock actual del producto aumenta en la cantidad ingresada y el costo
  de última compra se reemplaza por el costo unitario de esa entrada.
- **Condiciones:** la actualización se realiza de forma conjunta con el registro del movimiento.
- **Consecuencia:** el stock y el costo de última compra quedan actualizados con los datos de la entrada confirmada.

### RN204 — Registro de consumos

- **Enunciado:** El mecánico registra directamente los productos utilizados en el taller, sin necesidad de una
  solicitud previa al Jefe de Taller. El Jefe de Taller también puede registrar consumos.
- **Condiciones:** cada consumo queda asociado al usuario que lo registra y a la fecha del registro.
- **Consecuencia:** el consumo no autorizado se rechaza y no descuenta stock.

### RN205 — Contenido y trazabilidad del consumo

- **Enunciado:** Cada consumo identifica al usuario que lo registró, la fecha, los productos, las cantidades y la
  finalidad. La referencia al vehículo o cliente es opcional; la finalidad es obligatoria.
- **Condiciones:** el consumo debe incluir al menos un producto y cada producto debe indicar una cantidad. La
  finalidad describe el uso dado a los productos.
- **Consecuencia:** se rechaza el consumo incompleto y no se descuenta stock.
- **Trazabilidad:** las longitudes y obligatoriedades de los campos se detallan en
  [VALIDACIONES.md](VALIDACIONES.md).

### RN206 — Disponibilidad y no negatividad del stock

- **Enunciado:** Cada producto consumido debe existir, estar activo y tener stock suficiente para la cantidad
  solicitada. Ninguna operación puede dejar el stock en negativo.
- **Condiciones:** la cantidad solicitada de cada producto debe ser mayor que cero. Si un producto se repite en
  varios ítems del mismo consumo, la disponibilidad se evalúa sobre la cantidad total solicitada.
- **Consecuencia:** se rechaza el consumo y no se descuenta stock; el inventario nunca queda negativo.
- **Trazabilidad:** las validaciones de cantidad y de producto se detallan en [VALIDACIONES.md](VALIDACIONES.md).

### RN207 — Atomicidad del consumo

- **Enunciado:** El registro de un consumo es atómico: si algún ítem no cumple las condiciones, no se descuenta
  stock de ninguno de los productos del consumo.
- **Condiciones:** la operación se confirma completa o se rechaza completa; no se admiten descuentos parciales.
- **Consecuencia:** ante cualquier fallo, el inventario permanece en el estado anterior al intento de consumo.
- **Trazabilidad:** los mecanismos de transacción, agrupación y bloqueo que garantizan esta regla se documentan en
  [CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md).

### RN208 — Trazabilidad de movimientos

- **Enunciado:** Cada movimiento confirmado conserva el tipo de movimiento, el producto, la cantidad o variación,
  el stock anterior y posterior, la fecha, el usuario y el motivo o justificación.
- **Condiciones:** la información se conserva como parte del historial del inventario y no se elimina al
  desactivar el producto.
- **Consecuencia:** cualquier movimiento confirmado puede auditarse y reconstruir el efecto producido sobre el
  stock.

### RN209 — Ajustes de inventario

- **Enunciado:** Solo el Jefe de Taller realiza ajustes de inventario. El ajuste fija el stock en la cantidad física
  contada, que no puede ser negativa, y exige una justificación. La variación es la diferencia entre la cantidad
  física y el stock anterior. El ajuste no modifica el costo de última compra.
- **Condiciones:** el producto debe existir y estar activo; la justificación debe explicar el motivo del ajuste.
- **Consecuencia:** se rechaza el ajuste inválido y no se modifica el stock ni el costo.
- **Trazabilidad:** los rangos y longitudes se detallan en [VALIDACIONES.md](VALIDACIONES.md).

## 4. Control y abastecimiento

### RN301 — Stock mínimo configurado

- **Enunciado:** Todo producto activo debe tener configurado un stock mínimo. El stock mínimo es obligatorio al
  crear el producto y no puede ser negativo.
- **Condiciones:** el stock mínimo representa el nivel a partir del cual el producto se considera en riesgo de
  desabastecimiento.
- **Consecuencia:** no se crea un producto sin stock mínimo; un producto activo siempre cuenta con ese parámetro.
- **Trazabilidad:** la restricción de obligatoriedad y rango se detalla en [VALIDACIONES.md](VALIDACIONES.md).

### RN302 — Condición de bajo stock

- **Enunciado:** Hay bajo stock cuando el stock actual es menor o igual al stock mínimo. Solo se consideran los
  productos activos.
- **Condiciones:** el stock actual y el stock mínimo se comparan en la misma unidad de medida del producto.
- **Consecuencia:** el producto se incluye en las alertas y en los reportes de bajo stock; si el stock supera el
  mínimo, deja de considerarse en riesgo.
- **Trazabilidad:** la exposición del indicador `stockCritico` en las respuestas se especifica en
  [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md).

### RN305 — Cantidad sugerida de reposición

- **Enunciado:** La cantidad sugerida es la diferencia entre el stock mínimo y el stock actual
  (`stockMinimo - stockActual`). Es un valor referencial para la reposición.
- **Condiciones:** se calcula únicamente para productos en condición de bajo stock.
- **Consecuencia:** la cantidad sugerida orienta la reposición, pero no genera por sí sola una orden de compra ni un
  movimiento de inventario.
- **Trazabilidad:** el orden de presentación de las alertas se especifica en
  [REQUISITOS_FUNCIONALES.md](REQUISITOS_FUNCIONALES.md).

## 5. Consultas, reportes y valoración

### RN407 — Valor estimado del inventario

- **Enunciado:** El valor estimado del inventario es la suma de `stockActual * costoUltimaCompra` de los productos
  activos. Es una estimación operativa, no un valor contable oficial.
- **Condiciones:** solo se consideran productos activos y con costo de última compra registrado.
- **Consecuencia:** el resultado informa el valor estimado del inventario; no sustituye una valoración contable.

### RN408 — Integridad y conservación de reportes

- **Enunciado:** Los reportes son de solo lectura y se basan en movimientos confirmados. Conservan la consulta
  histórica aunque el producto esté inactivo. Si un producto no tiene costo de última compra registrado, se
  identifica, se excluye del total y se informa la cantidad de productos excluidos.
- **Condiciones:** la consulta no modifica datos. El valor estimado del inventario solo considera productos activos;
  los reportes de movimientos y consumos conservan el histórico de productos inactivos.
- **Consecuencia:** el total excluye los productos sin costo y la respuesta informa cuántos quedaron fuera; la
  información histórica permanece consultable.
- **Trazabilidad:** el mecanismo de transacción de solo lectura se documenta en
  [CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md).

## 6. Documentos relacionados

- [VALIDACIONES.md](VALIDACIONES.md): restricciones de entrada, formatos, longitudes, rangos y unicidad.
- [POLITICAS_DE_SEGURIDAD.md](POLITICAS_DE_SEGURIDAD.md): autenticación, autorización, roles, permisos y tokens.
- [CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md): JWT, zona horaria, tareas programadas, transacciones y
  persistencia.
- [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md): endpoints, parámetros, paginación, filtros, respuestas y códigos
  HTTP.
- [REQUISITOS_FUNCIONALES.md](REQUISITOS_FUNCIONALES.md): alertas, reportes, dashboard, estadísticas y
  exportaciones.
