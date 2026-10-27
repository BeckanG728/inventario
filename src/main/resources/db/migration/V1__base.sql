CREATE TABLE rol (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    rol_id      BIGINT       NOT NULL REFERENCES rol (id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO rol (nombre) VALUES ('ADMIN'), ('JEFE_TALLER'), ('MECANICO');