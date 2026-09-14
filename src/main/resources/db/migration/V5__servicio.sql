-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Servicios/Mantenimientos + checklist por categoria
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V5__servicio.sql
-- =============================================================

-- Catalogo de items de checklist por categoria (no columnas fijas: el
-- checklist de un TRAILER es una lista de filas distinta a la de una
-- MAQUINARIA, gestionable sin deploy).
CREATE TABLE checklist_item_catalogo (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    categoria  ENUM('EQUIPO_MENOR','EQUIPO_MAYOR','VEHICULO','TRAILER','MAQUINARIA') NOT NULL,
    nombre     VARCHAR(200) NOT NULL,
    orden      INT          NOT NULL DEFAULT 0,
    activo     TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_checklist_categoria ON checklist_item_catalogo(categoria);

CREATE TABLE servicio (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    equipo_id               BIGINT UNSIGNED NOT NULL,
    tipo_servicio           ENUM('ALISTAMIENTO','PREVENTIVO','CORRECTIVO') NOT NULL,
    descripcion             TEXT          NOT NULL,
    fecha                   DATE          NOT NULL,
    costo_valorizado        DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    tecnico_responsable_id  BIGINT UNSIGNED DEFAULT NULL,
    creado_en               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_servicio_equipo  FOREIGN KEY (equipo_id) REFERENCES equipo(id) ON DELETE RESTRICT,
    CONSTRAINT fk_servicio_tecnico FOREIGN KEY (tecnico_responsable_id) REFERENCES usuario(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_servicio_equipo  ON servicio(equipo_id);
CREATE INDEX idx_servicio_fecha   ON servicio(fecha);
CREATE INDEX idx_servicio_tecnico ON servicio(tecnico_responsable_id);

CREATE TABLE servicio_repuesto (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    servicio_id  BIGINT UNSIGNED NOT NULL,
    repuesto_id  BIGINT UNSIGNED NOT NULL,
    cantidad     DECIMAL(12,2) NOT NULL DEFAULT 1.00,

    CONSTRAINT fk_servrep_servicio  FOREIGN KEY (servicio_id) REFERENCES servicio(id) ON DELETE CASCADE,
    CONSTRAINT fk_servrep_repuesto  FOREIGN KEY (repuesto_id) REFERENCES repuesto(id) ON DELETE RESTRICT,
    CONSTRAINT uq_servicio_repuesto UNIQUE (servicio_id, repuesto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE servicio_checklist_respuesta (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    servicio_id  BIGINT UNSIGNED NOT NULL,
    item_id      BIGINT UNSIGNED NOT NULL,
    completado   TINYINT(1)   NOT NULL DEFAULT 0,
    observacion  VARCHAR(300) DEFAULT NULL,

    CONSTRAINT fk_checkresp_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkresp_item     FOREIGN KEY (item_id) REFERENCES checklist_item_catalogo(id) ON DELETE RESTRICT,
    CONSTRAINT uq_servicio_item      UNIQUE (servicio_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
