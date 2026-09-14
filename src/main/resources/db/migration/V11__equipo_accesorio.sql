-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Accesorios por Equipo (catalogo) + seleccion en Movimientos
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V11__equipo_accesorio.sql
--
--  equipo_accesorio es el catalogo de accesorios propios de cada equipo
--  (ej. bateria, cargador, maletin), independiente de la columna de texto
--  libre "accesorios" que ya existia en equipo (esa sigue siendo solo una
--  nota general para equipos externos).
--
--  orden_salida_item_accesorio registra, por cada equipo dentro de una
--  orden de salida, cuales de sus accesorios cataloga van fisicamente con
--  el (los que no se marcan se asumen que se quedan en el almacen).
-- =============================================================

CREATE TABLE equipo_accesorio (
    id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    equipo_id BIGINT UNSIGNED NOT NULL,
    nombre    VARCHAR(150) NOT NULL,
    cantidad  INT UNSIGNED NOT NULL DEFAULT 1,

    CONSTRAINT fk_accesorio_equipo FOREIGN KEY (equipo_id) REFERENCES equipo(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orden_salida_item_accesorio (
    orden_salida_item_id  BIGINT UNSIGNED NOT NULL,
    equipo_accesorio_id   BIGINT UNSIGNED NOT NULL,

    PRIMARY KEY (orden_salida_item_id, equipo_accesorio_id),
    CONSTRAINT fk_item_accesorio_item FOREIGN KEY (orden_salida_item_id) REFERENCES orden_salida_item(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_accesorio_accesorio FOREIGN KEY (equipo_accesorio_id) REFERENCES equipo_accesorio(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
