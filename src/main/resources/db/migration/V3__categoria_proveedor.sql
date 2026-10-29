CREATE TABLE categoria (
                           id           BIGSERIAL    PRIMARY KEY,
                           nombre       VARCHAR(100) NOT NULL,
                           descripcion  VARCHAR(255),
                           activo       BOOLEAN      NOT NULL DEFAULT TRUE
);
-- nombre único sin distinguir mayúsculas ("Filtros" = "filtros")
CREATE UNIQUE INDEX uq_categoria_nombre ON categoria (LOWER(nombre));

CREATE TABLE proveedor (
                           id            BIGSERIAL    PRIMARY KEY,
                           ruc           VARCHAR(20)  NOT NULL UNIQUE,
                           razon_social  VARCHAR(150) NOT NULL,
                           contacto      VARCHAR(100),
                           telefono      VARCHAR(30),
                           correo        VARCHAR(150),
                           direccion     VARCHAR(255),
                           activo        BOOLEAN      NOT NULL DEFAULT TRUE
);