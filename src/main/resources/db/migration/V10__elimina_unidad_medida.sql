-- V10: retira la columna de texto unidad_medida, reemplazada por unidad_base_id en V9,
-- y siembra como dato de negocio las 10 categorías del taller, las unidades de inventario que
-- necesitan y sus relaciones categoría-unidad.
-- V9 se mantuvo aditiva para permitir el backfill; con V10 el catálogo de unidades
-- (tabla unidad) es la única fuente de verdad de la unidad del producto.

ALTER TABLE producto DROP COLUMN unidad_medida;

-- Categorías base del taller (dato de negocio, como los roles en V1).
INSERT INTO categoria (nombre, descripcion, activo)
SELECT v.nombre, v.descripcion, TRUE
FROM (VALUES
    ('Lubricantes y aceites',     'Aceite de motor, aceite de transmisión'),
    ('Refrigerantes y líquidos',  'Refrigerante, líquido de frenos'),
    ('Grasas',                    'Grasa multipropósito, grasa de litio'),
    ('Filtros',                   'Filtro de aceite, de aire y de combustible'),
    ('Frenos',                    'Pastillas, zapatas y discos'),
    ('Componentes eléctricos',    'Fusibles, relés y bombillas'),
    ('Tornillería y fijaciones',  'Pernos, tuercas y arandelas'),
    ('Abrasivos',                 'Lijas, discos de corte y de desbaste'),
    ('Limpieza y mantenimiento',  'Desengrasantes y limpiacontactos'),
    ('Adhesivos y selladores',    'Silicona, pegamento y sellador de juntas')
) AS v(nombre, descripcion)
WHERE NOT EXISTS (SELECT 1 FROM categoria c WHERE LOWER(c.nombre) = LOWER(v.nombre));

-- Regla de unidades (permite_fragmentacion):
--   FALSE = unidad base, la que se almacena en el stock. Es una unidad discreta cuyo sobrante se puede
--           contar y ajustar sin ambigüedad: UND, o el envase de un producto líquido, graso o pastoso
--           (GALON, BOTELLA, TUBO...). El volumen o el peso (litro, mililitro, gramo) nunca es base,
--           porque lo que sobra de un envase abierto no se puede registrar con exactitud.
--   TRUE  = presentación, un agrupador de unidades base con factor entero (CAJA de 20 UND, JUEGO de 4 UND).
--           El sobrante se ajusta fácilmente en unidades base.
-- Unidades que V9 no sembró:
INSERT INTO unidad (codigo, nombre, permite_fragmentacion) VALUES
    ('BIDON',    'Bidón',    FALSE),
    ('BOTELLA',  'Botella',  FALSE),
    ('TUBO',     'Tubo',     FALSE),
    ('POTE',     'Pote',     FALSE),
    ('BALDE',    'Balde',    FALSE),
    ('AEROSOL',  'Aerosol',  FALSE),
    ('CARTUCHO', 'Cartucho', FALSE),
    ('PAQUETE',  'Paquete',  TRUE),
    ('BOLSA',    'Bolsa',    TRUE),
    ('BLISTER',  'Blíster',  TRUE)
ON CONFLICT (codigo) DO NOTHING;

-- Unidades de V9 que en este catálogo actúan como presentación.
UPDATE unidad SET permite_fragmentacion = TRUE
WHERE codigo IN ('CAJA', 'JUEGO', 'PAR');

-- Relaciones categoria_unidad: unidades base y presentaciones de cada categoría.
-- Esta es la única fuente de las relaciones categoría-unidad: ni el usuario ni el demo las crean.
-- LITRO, KG, KIT, METRO, CM, ROLLO y LATA siguen sembradas en V9 pero no se asocian a ninguna categoría.
INSERT INTO categoria_unidad (categoria_id, unidad_id)
SELECT c.id, u.id
FROM (VALUES
    ('Lubricantes y aceites',    'GALON'),   ('Lubricantes y aceites',    'BOTELLA'),
    ('Lubricantes y aceites',    'BIDON'),   ('Lubricantes y aceites',    'CAJA'),

    ('Refrigerantes y líquidos', 'BOTELLA'), ('Refrigerantes y líquidos', 'GALON'),
    ('Refrigerantes y líquidos', 'CAJA'),

    ('Grasas',                   'TUBO'),    ('Grasas',                   'POTE'),
    ('Grasas',                   'BALDE'),   ('Grasas',                   'CAJA'),

    ('Filtros',                  'UND'),     ('Filtros',                  'CAJA'),
    ('Filtros',                  'PAQUETE'),

    ('Frenos',                   'UND'),     ('Frenos',                   'JUEGO'),
    ('Frenos',                   'PAR'),     ('Frenos',                   'CAJA'),

    ('Componentes eléctricos',   'UND'),     ('Componentes eléctricos',   'CAJA'),
    ('Componentes eléctricos',   'PAQUETE'), ('Componentes eléctricos',   'BLISTER'),

    ('Tornillería y fijaciones', 'UND'),     ('Tornillería y fijaciones', 'CAJA'),
    ('Tornillería y fijaciones', 'BOLSA'),   ('Tornillería y fijaciones', 'PAQUETE'),

    ('Abrasivos',                'UND'),     ('Abrasivos',                'PAQUETE'),
    ('Abrasivos',                'CAJA'),

    ('Limpieza y mantenimiento', 'AEROSOL'), ('Limpieza y mantenimiento', 'GALON'),
    ('Limpieza y mantenimiento', 'CAJA'),

    ('Adhesivos y selladores',   'CARTUCHO'), ('Adhesivos y selladores',  'TUBO'),
    ('Adhesivos y selladores',   'POTE'),     ('Adhesivos y selladores',  'CAJA')
) AS rel(categoria, unidad)
JOIN categoria c ON LOWER(c.nombre) = LOWER(rel.categoria)
JOIN unidad u ON u.codigo = rel.unidad
ON CONFLICT (categoria_id, unidad_id) DO NOTHING;
