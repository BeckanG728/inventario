-- V10: retira la columna de texto unidad_medida, reemplazada por unidad_base_id en V9,
-- y siembra como dato de negocio las categorías base y sus unidades coherentes.
-- V9 se mantuvo aditiva para permitir el backfill; con V10 el catálogo de unidades
-- (tabla unidad) es la única fuente de verdad de la unidad del producto.

ALTER TABLE producto DROP COLUMN unidad_medida;

-- Categorías base del taller (dato de negocio, como los roles en V1).
INSERT INTO categoria (nombre, descripcion, activo)
SELECT v.nombre, NULL, TRUE
FROM (VALUES
    ('Lubricantes'), ('Filtros'), ('Frenos'), ('Suspensión'), ('Motor'),
    ('Eléctrico'), ('Transmisión'), ('Neumáticos'), ('Herramientas'), ('Consumibles'),
    ('Carrocería'), ('Refrigeración'), ('Escape'), ('Iluminación'), ('Seguridad'),
    ('Dirección'), ('Climatización'), ('Limpieza'), ('Pintura'), ('Accesorios')
) AS v(nombre)
WHERE NOT EXISTS (SELECT 1 FROM categoria c WHERE LOWER(c.nombre) = LOWER(v.nombre));

-- Relaciones categoria_unidad coherentes. No se crean unidades: ya existen desde V9.
-- KG se siembra sin categoría (decisión acordada); por eso no aparece aquí.
-- El contenido envases de 1 L / 250 g se controla como UND y la longitud como CM.
INSERT INTO categoria_unidad (categoria_id, unidad_id)
SELECT c.id, u.id
FROM (VALUES
    ('Lubricantes',   'LITRO'), ('Lubricantes',   'GALON'),
    ('Filtros',       'UND'),
    ('Frenos',        'JUEGO'), ('Frenos',        'UND'), ('Frenos', 'PAR'), ('Frenos', 'LITRO'),
    ('Suspensión',    'UND'),   ('Suspensión',    'PAR'),
    ('Motor',         'UND'),   ('Motor',         'CAJA'), ('Motor',  'KIT'),
    ('Eléctrico',     'UND'),   ('Eléctrico',     'CAJA'), ('Eléctrico', 'PAR'),
    ('Transmisión',   'JUEGO'), ('Transmisión',   'KIT'), ('Transmisión', 'LITRO'), ('Transmisión', 'GALON'),
    ('Neumáticos',    'UND'),
    ('Herramientas',  'UND'),   ('Herramientas',  'JUEGO'),
    ('Consumibles',   'UND'),   ('Consumibles',   'LITRO'), ('Consumibles', 'ROLLO'), ('Consumibles', 'CAJA'),
    ('Carrocería',    'UND'),   ('Carrocería',    'PAR'),
    ('Refrigeración', 'UND'),   ('Refrigeración', 'METRO'), ('Refrigeración', 'CM'),
    ('Escape',        'UND'),
    ('Iluminación',   'UND'),   ('Iluminación',   'PAR'),
    ('Seguridad',     'UND'),
    ('Dirección',     'UND'),   ('Dirección',     'PAR'),
    ('Climatización', 'UND'),   ('Climatización', 'LATA'),
    ('Limpieza',      'LITRO'), ('Limpieza',      'GALON'),
    ('Pintura',       'UND'),
    ('Accesorios',    'UND'),   ('Accesorios',    'PAR'), ('Accesorios', 'JUEGO')
) AS rel(categoria, unidad)
JOIN categoria c ON LOWER(c.nombre) = LOWER(rel.categoria)
JOIN unidad u ON u.codigo = rel.unidad
ON CONFLICT (categoria_id, unidad_id) DO NOTHING;
