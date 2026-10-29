CREATE TABLE categoria (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE proveedor (
    id           BIGSERIAL PRIMARY KEY,
    ruc          VARCHAR(20)  NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    contacto     VARCHAR(150),
    telefono     VARCHAR(30),
    correo       VARCHAR(150),
    direccion    VARCHAR(255),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE producto (
    id                  BIGSERIAL PRIMARY KEY,
    codigo              VARCHAR(50)    NOT NULL UNIQUE,
    sku                 VARCHAR(50)    UNIQUE,
    nombre              VARCHAR(150)   NOT NULL,
    descripcion         VARCHAR(500),
    unidad_medida       VARCHAR(30)    NOT NULL,
    stock_actual        INTEGER        NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo        INTEGER        NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    costo_ultima_compra NUMERIC(12, 2),
    activo              BOOLEAN        NOT NULL DEFAULT TRUE,
    categoria_id        BIGINT         NOT NULL REFERENCES categoria (id),
    proveedor_id        BIGINT         NOT NULL REFERENCES proveedor (id)
);

CREATE INDEX idx_producto_categoria ON producto (categoria_id);
CREATE INDEX idx_producto_proveedor ON producto (proveedor_id);

CREATE TABLE movimiento_inventario (
    id              BIGSERIAL PRIMARY KEY,
    fecha           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    tipo            VARCHAR(20)    NOT NULL CHECK (tipo IN ('ENTRADA', 'CONSUMO', 'AJUSTE')),
    cantidad        INTEGER,
    variacion       INTEGER,
    stock_anterior  INTEGER        NOT NULL,
    stock_posterior INTEGER        NOT NULL,
    costo_unitario  NUMERIC(12, 2),
    motivo          VARCHAR(255),
    producto_id     BIGINT         NOT NULL REFERENCES producto (id),
    consumo_id      BIGINT,
    usuario_id      BIGINT         NOT NULL REFERENCES usuario (id)
);

CREATE INDEX idx_movimiento_producto ON movimiento_inventario (producto_id);
CREATE INDEX idx_movimiento_fecha ON movimiento_inventario (fecha);
