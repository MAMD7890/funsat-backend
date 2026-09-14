-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Auditoria: registro de quien creo/edito/elimino que, y cuando.
--  Se escribe explicitamente desde cada servicio (no via listeners
--  automaticos de Hibernate), para tener control total sobre la
--  descripcion legible de cada evento.
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V14
-- =============================================================

CREATE TABLE auditoria (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_username  VARCHAR(60)  DEFAULT NULL,
    usuario_nombre    VARCHAR(120) DEFAULT NULL,
    accion            ENUM('CREAR','EDITAR','ELIMINAR') NOT NULL,
    entidad           VARCHAR(60)  NOT NULL,
    entidad_id        BIGINT UNSIGNED DEFAULT NULL,
    descripcion       VARCHAR(300) DEFAULT NULL,
    fecha             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_auditoria_fecha    ON auditoria(fecha);
CREATE INDEX idx_auditoria_entidad  ON auditoria(entidad, entidad_id);
CREATE INDEX idx_auditoria_usuario  ON auditoria(usuario_username);
