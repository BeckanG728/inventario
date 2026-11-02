CREATE TABLE consumo (
    id                             BIGSERIAL    PRIMARY KEY,
    fecha                          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    finalidad                      VARCHAR(255) NOT NULL CHECK (LENGTH(TRIM(finalidad)) > 0),
    referencia_vehiculo_cliente    VARCHAR(150),
    usuario_id                     BIGINT       NOT NULL REFERENCES usuario (id)
);

CREATE INDEX idx_consumo_fecha ON consumo (fecha);
CREATE INDEX idx_consumo_usuario ON consumo (usuario_id);

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT fk_movimiento_consumo
        FOREIGN KEY (consumo_id) REFERENCES consumo (id);
