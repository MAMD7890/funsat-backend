-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Repuestos (catalogo)
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V4__repuesto.sql
-- =============================================================

CREATE TABLE repuesto (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(160)  NOT NULL,
    codigo            VARCHAR(60)   DEFAULT NULL,
    costo_unitario    DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    stock_disponible  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    creado_en         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_repuesto_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_repuesto_nombre ON repuesto(nombre);
