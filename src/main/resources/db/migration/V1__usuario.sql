-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Autenticacion y seguridad
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V1__usuario.sql
-- =============================================================

CREATE TABLE usuario (
    id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre                 VARCHAR(120) NOT NULL,
    username               VARCHAR(60)  NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    rol                    ENUM('ADMIN','SUPERVISOR','TECNICO') NOT NULL DEFAULT 'TECNICO',
    activo                 TINYINT(1)   NOT NULL DEFAULT 1,
    debe_cambiar_password  TINYINT(1)   NOT NULL DEFAULT 0,
    creado_en              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_usuario_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
