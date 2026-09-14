-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Clientes + Equipo Master unificado (propio / externo)
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V3__cliente_equipo.sql
-- =============================================================

CREATE TABLE cliente (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(160) NOT NULL,
    tipo            ENUM('NATURAL','EMPRESA') NOT NULL,
    telefono        VARCHAR(30)  DEFAULT NULL,
    email           VARCHAR(120) DEFAULT NULL,
    creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- Equipo unificado: mismo modelo para equipos propios
-- (Inventario_Master.xlsx) y equipos externos de clientes en
-- taller (Inventario_SERVICIO_TALLER.xlsx), diferenciados por
-- "propiedad". estado = estado fisico/operativo del equipo;
-- estado_servicio_taller = estado del flujo de servicio dentro
-- del taller. Son independientes a proposito, no se mezclan.
-- =============================================================

CREATE TABLE equipo (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,

    codigo                  VARCHAR(30)  DEFAULT NULL,
    codigo_taller           VARCHAR(20)  DEFAULT NULL,
    descripcion_equipo      VARCHAR(200) NOT NULL,
    categoria               ENUM('EQUIPO_MENOR','EQUIPO_MAYOR','VEHICULO','TRAILER','MAQUINARIA') NOT NULL,
    marca                   VARCHAR(100) DEFAULT NULL,
    numero_serie            VARCHAR(100) DEFAULT NULL,
    modelo                  VARCHAR(100) DEFAULT NULL,
    motor                   ENUM('GASOLINA_2T','GASOLINA_4T','DIESEL','ELECTRICO_110V','ELECTRICO_220V','HIDRAULICO','SIN_MOTOR') DEFAULT NULL,
    ubicacion               VARCHAR(160) DEFAULT NULL,
    fecha_registro          DATE         NOT NULL DEFAULT (CURRENT_DATE),

    estado                  ENUM('ACTIVO','INACTIVO','EN_MANTENIMIENTO','DADO_DE_BAJA') NOT NULL DEFAULT 'ACTIVO',

    check_mtto              TINYINT(1)   NOT NULL DEFAULT 0,
    check_hv                TINYINT(1)   NOT NULL DEFAULT 0,
    check_ft                TINYINT(1)   NOT NULL DEFAULT 0,

    propiedad               ENUM('PROPIO','EXTERNO') NOT NULL DEFAULT 'PROPIO',
    cliente_id              BIGINT UNSIGNED DEFAULT NULL,

    accesorios              TEXT         DEFAULT NULL,
    fecha_ingreso           DATE         DEFAULT NULL,
    fecha_diagnostico       DATE         DEFAULT NULL,
    rotulado                TINYINT(1)   NOT NULL DEFAULT 0,

    estado_servicio_taller  ENUM('PENDIENTE_POR_DIAGNOSTICO','POR_INICIAR_SERVICIO','POR_APROBAR',
                                  'POR_DEFINIR','STAND_BY','SIN_CANCELAR_Y_REPARADO','PENDIENTE_REMISION',
                                  'PENDIENTE_POR_FACTURA_Y_ENTREGA','REPARADA_POR_RETIRAR') DEFAULT NULL,

    creado_en               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_equipo_codigo        UNIQUE (codigo),
    CONSTRAINT uq_equipo_codigo_taller UNIQUE (codigo_taller),
    CONSTRAINT fk_equipo_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_equipo_categoria     ON equipo(categoria);
CREATE INDEX idx_equipo_estado        ON equipo(estado);
CREATE INDEX idx_equipo_propiedad     ON equipo(propiedad);
CREATE INDEX idx_equipo_cliente       ON equipo(cliente_id);
CREATE INDEX idx_equipo_numero_serie  ON equipo(numero_serie);
