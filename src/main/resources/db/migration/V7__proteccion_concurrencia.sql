ALTER TABLE usuario ADD COLUMN version_sesion BIGINT NOT NULL DEFAULT 0;

CREATE TABLE bloqueo_operacion (
    nombre VARCHAR(40) PRIMARY KEY
);

INSERT INTO bloqueo_operacion (nombre) VALUES ('alta_producto'), ('administradores');

CREATE TABLE operacion_idempotente (
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    operacion VARCHAR(30) NOT NULL,
    clave UUID NOT NULL,
    huella VARCHAR(64) NOT NULL,
    respuesta TEXT,
    PRIMARY KEY (usuario_id, operacion, clave)
);
