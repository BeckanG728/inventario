# Integración con el frontend

Guía rápida para el proyecto `inventario-front`. El contrato completo está en
[CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md); las reglas en [REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md) y
[VALIDACIONES.md](VALIDACIONES.md).

## Organización del backend

Monolito modular bajo `com.taller`: `seguridad` (auth/usuarios), `catalogo` (categorías, proveedores, productos,
**unidades**), `inventario` (movimientos y consumos), `monitoreo`, `reportes`, `auditoria` e `inicializacion`.

Todas las respuestas paginadas usan `{ content, page, size, totalElements, totalPages }`. Los errores siguen el
contrato de [TABLA_ERRORES.md](TABLA_ERRORES.md) (`400 VALIDATION_ERROR` con `details[{field, reason}]`).

## Roles

`ADMIN` (semilla), `JEFE_TALLER` (escritura de catálogo, entradas y ajustes) y `MECANICO` (consumos y consultas).

## Endpoints por pantalla

| Pantalla | Endpoints |
|---|---|
| Listado de productos | `GET /api/productos?page&size&nombre&categoria&unidadBaseId&activo&stockCritico` |
| Formulario de producto | `GET /api/unidades`, `GET /api/categorias/{id}/unidades`, `POST/PUT /api/productos` |
| ABM de categorías | `GET /api/categorias`, `POST /api/categorias`, `PUT /api/categorias/{id}/unidades` |
| Entradas | `POST /api/movimientos/entrada` |
| Consumos | `GET /api/consumos/productos-frecuentes`, `POST /api/consumos` |
| Ajustes | `POST /api/movimientos/ajuste` |
| Detalle / existencias | `GET /api/monitoreo/existencias` |

## Migración de contratos V9 → V10

| Antes (V1–V8) | Ahora (V9/V10) |
|---|---|
| `unidadMedida: string` en ProductoResponse y DTOs | Eliminado. Usar `unidadBaseId`, `unidadBaseCodigo`, `unidadBaseNombre` |
| Filtro `?unidadMedida=UNIDAD` | Filtro `?unidadBaseId=1` |
| Presentación implícita en el texto | `unidadPresentacionId`, `unidadPresentacionCodigo`, `factorPresentacion` |
| `cantidad` siempre entera en la unidad del producto | `cantidad` en unidad de registro + `cantidadBase` en unidad base; decimal solo en longitud |

`CreateProductoRequest` / `UpdateProductoRequest` (V10):

```json
{
  "nombre": "Bujía NGK",
  "descripcion": "Bujía de encendido",
  "unidadBaseId": 1,
  "unidadPresentacionId": 2,
  "factorPresentacion": 20,
  "stockMinimo": 10,
  "id_categoria": 5,
  "id_proveedor": 7
}
```

`EntradaRequest` con presentación:

```json
{
  "productoId": 1,
  "proveedorId": 7,
  "cantidad": 3,
  "unidadRegistro": "CAJA",
  "factorAplicado": 20,
  "costoUnitario": 25.00
}
```

Respuesta: `cantidad=60`, `cantidadBase=60`, `variacion=60`, `costoUnitario=1.25`, `stockAnterior=0`,
`stockPosterior=60`. `MovimientoResponse` incluye `unidadRegistro`, `factorAplicado` y `cantidadBase` como snapshot.

## Reglas que debe respetar el frontend

- El combobox de unidades del formulario de producto se alimenta de `GET /api/categorias/{id}/unidades`; no debe
  hardcodearse. `KG` está en el catálogo pero sin categoría asignada: no aparecerá en ese combobox.
- El alta de categoría exige `unidadIds` no vacío.
- El selector de presentación solo se muestra si la unidad tiene `permiteFragmentacion=true` (`CAJA`, `METRO`, `ROLLO`).
- El campo decimal (`cantidadExacta` / `cantidadFisicaExacta`) solo aplica a `METRO` y `ROLLO`; el backend lo rechaza
  para cualquier otra unidad.
- La unidad base de un producto con movimientos no se puede cambiar; solo su presentación y factor.
