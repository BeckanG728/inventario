CREATE TABLE auditoria_operacion (
    id             BIGSERIAL    PRIMARY KEY,
    fecha          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    tipo           VARCHAR(20)  NOT NULL,
    actor_id       BIGINT,
    actor_username VARCHAR(50)  NOT NULL,
    metodo         VARCHAR(10)  NOT NULL,
    endpoint       VARCHAR(500) NOT NULL,
    recurso        VARCHAR(100),
    recurso_id     VARCHAR(100),
    resultado      VARCHAR(50)  NOT NULL,
    exito          BOOLEAN      NOT NULL
);

CREATE INDEX idx_auditoria_fecha ON auditoria_operacion (fecha);
CREATE INDEX idx_auditoria_actor ON auditoria_operacion (actor_id, fecha);
CREATE INDEX idx_auditoria_tipo ON auditoria_operacion (tipo, fecha);
CREATE INDEX idx_auditoria_resultado ON auditoria_operacion (resultado);

/** La auditoría es append-only: no se permite actualizar ni borrar asientos. */
CREATE OR REPLACE FUNCTION fn_auditoria_solo_insercion()
    RETURNS TRIGGER AS
$$
BEGIN
    RAISE EXCEPTION 'La tabla de auditoría es de solo inserción';
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER trg_auditoria_solo_insercion
    BEFORE UPDATE OR DELETE ON auditoria_operacion
    FOR EACH ROW
EXECUTE FUNCTION fn_auditoria_solo_insercion();
