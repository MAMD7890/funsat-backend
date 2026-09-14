-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Fichas de Mantenimiento - Evidencia Fotografica
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V6__evidencia_fotografica.sql
-- =============================================================

CREATE TABLE evidencia_fotografica (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    servicio_id     BIGINT UNSIGNED NOT NULL,
    tipo_evidencia  ENUM('ANTES','DESPUES') NOT NULL,
    ruta_archivo    VARCHAR(500) NOT NULL,
    nombre_original VARCHAR(255) DEFAULT NULL,
    content_type    VARCHAR(100) DEFAULT NULL,
    tamano_bytes    BIGINT UNSIGNED DEFAULT NULL,
    descripcion     VARCHAR(300) DEFAULT NULL,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_evidencia_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_evidencia_servicio ON evidencia_fotografica(servicio_id);
