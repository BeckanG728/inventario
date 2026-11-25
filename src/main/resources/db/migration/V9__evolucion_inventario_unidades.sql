-- V9: evolución del modelo de inventario y unidades de medida.
-- V1-V8 permanecen inmutables. Todas las operaciones son aditivas; unidad_medida se conserva
-- para compatibilidad con los clientes actuales y se retirará en una migración posterior.

CREATE TABLE unidad (
    id                    BIGSERIAL    PRIMARY KEY,
    codigo                VARCHAR(20)  NOT NULL UNIQUE,
    nombre                VARCHAR(60)  NOT NULL,
    permite_fragmentacion BOOLEAN      NOT NULL DEFAULT FALSE,
    activo                BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Semilla de negocio (como los roles en V1): no depende del perfil demo.
INSERT INTO unidad (codigo, nombre, permite_fragmentacion) VALUES
    ('UND',   'Unidad',     FALSE),
    ('CAJA',  'Caja',       TRUE),
    ('PAR',   'Par',        FALSE),
    ('JUEGO', 'Juego',      FALSE),
    ('KIT',   'Kit',        FALSE),
    ('LITRO', 'Litro',      FALSE),
    ('GALON', 'Galón',      FALSE),
    ('KG',    'Kilogramo',  FALSE),
    ('METRO', 'Metro',      TRUE),
    ('CM',    'Centímetro', FALSE),
    ('ROLLO', 'Rollo',      TRUE),
    ('LATA',  'Lata',       FALSE)
ON CONFLICT (codigo) DO NOTHING;

CREATE TABLE categoria_unidad (
    categoria_id BIGINT NOT NULL REFERENCES categoria (id),
    unidad_id    BIGINT NOT NULL REFERENCES unidad (id),
    PRIMARY KEY (categoria_id, unidad_id)
);

CREATE INDEX idx_categoria_unidad_unidad ON categoria_unidad (unidad_id);

ALTER TABLE producto
    ADD COLUMN unidad_base_id         BIGINT REFERENCES unidad (id),
    ADD COLUMN unidad_presentacion_id BIGINT REFERENCES unidad (id),
    ADD COLUMN factor_presentacion    INTEGER CHECK (factor_presentacion > 0);

-- Backfill: la unidad base es la unidad mínima consumible.
-- CAJA y KILOGRAMO pasan a UND; METRO/ROLLO pasan a CM (longitud entera en centímetros).
UPDATE producto p
SET unidad_base_id = u.id
FROM unidad u
WHERE u.codigo = CASE UPPER(TRIM(p.unidad_medida))
    WHEN 'UNIDAD'    THEN 'UND'
    WHEN 'CAJA'      THEN 'UND'
    WHEN 'KILOGRAMO' THEN 'UND'
    WHEN 'METRO'     THEN 'CM'
    WHEN 'ROLLO'     THEN 'CM'
    ELSE UPPER(TRIM(p.unidad_medida))
END;

-- Presentación habitual y factor por defecto para los productos que antes se controlaban en CAJA/METRO/ROLLO.
UPDATE producto p
SET unidad_presentacion_id = u.id,
    factor_presentacion = 100
FROM unidad u
WHERE u.codigo = 'METRO' AND UPPER(TRIM(p.unidad_medida)) = 'METRO';

UPDATE producto p
SET unidad_presentacion_id = u.id
FROM unidad u
WHERE u.codigo = 'ROLLO' AND UPPER(TRIM(p.unidad_medida)) = 'ROLLO';

UPDATE producto p
SET unidad_presentacion_id = u.id,
    factor_presentacion = 20
FROM unidad u
WHERE u.codigo = 'CAJA' AND UPPER(TRIM(p.unidad_medida)) = 'CAJA';

-- Fallo explícito si algún texto de unidad no pudo traducirse, antes de exigir NOT NULL.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM producto WHERE unidad_base_id IS NULL) THEN
        RAISE EXCEPTION 'V9: hay productos cuya unidad_medida no existe en el catálogo de unidades';
    END IF;
END $$;

ALTER TABLE producto ALTER COLUMN unidad_base_id SET NOT NULL;

CREATE INDEX idx_producto_unidad_base ON producto (unidad_base_id);

ALTER TABLE movimiento_inventario
    ADD COLUMN unidad_registro VARCHAR(20),
    ADD COLUMN factor_aplicado INTEGER CHECK (factor_aplicado > 0),
    ADD COLUMN cantidad_base    INTEGER CHECK (cantidad_base > 0);

-- Snapshots históricos: se conservan las cifras originales, sin multiplicar por el factor.
UPDATE movimiento_inventario m
SET cantidad_base = m.cantidad
WHERE m.cantidad IS NOT NULL;

UPDATE movimiento_inventario m
SET unidad_registro = CASE UPPER(TRIM(p.unidad_medida))
    WHEN 'UNIDAD'    THEN 'UND'
    WHEN 'CAJA'      THEN 'UND'
    WHEN 'KILOGRAMO' THEN 'UND'
    WHEN 'METRO'     THEN 'CM'
    WHEN 'ROLLO'     THEN 'CM'
    ELSE UPPER(TRIM(p.unidad_medida))
END
FROM producto p
WHERE p.id = m.producto_id;

-- Semántica explícita de cantidad / variación / cantidad_base por tipo de movimiento.
ALTER TABLE movimiento_inventario
    ADD CONSTRAINT ck_movimiento_cantidad_por_tipo CHECK (
        (tipo = 'AJUSTE' AND cantidad IS NULL)
        OR (tipo IN ('ENTRADA', 'CONSUMO') AND cantidad > 0)
    );

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT ck_movimiento_variacion_por_tipo CHECK (
        (tipo = 'CONSUMO' AND variacion IS NULL)
        OR (tipo IN ('ENTRADA', 'AJUSTE') AND variacion IS NOT NULL)
    );

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT ck_movimiento_cantidad_base_por_tipo CHECK (
        (tipo = 'AJUSTE' AND cantidad_base IS NULL)
        OR (tipo IN ('ENTRADA', 'CONSUMO') AND cantidad_base > 0)
    );

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT ck_movimiento_stock_no_negativo CHECK (stock_anterior >= 0 AND stock_posterior >= 0);
