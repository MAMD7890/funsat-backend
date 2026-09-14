-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Movimientos de Equipos (Ordenes de Salida / Entrada)
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V10__orden_salida.sql
--
--  Una orden de salida agrupa varios equipos que salen juntos hacia un
--  mismo cliente/destino (prestamo o alquiler entre empresas). La
--  "entrada" no es una tabla separada: cada item de la orden se cierra
--  individualmente registrando su propia fecha_devolucion, lo que permite
--  devoluciones parciales (algunos equipos vuelven antes que otros).
--  Mientras un item no tenga fecha_devolucion, ese equipo se considera
--  "en la calle" y no puede salir en una orden nueva (se valida en el
--  service, no aqui).
-- =============================================================

CREATE TABLE orden_salida (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fecha_salida      DATE NOT NULL,
    cliente_id        BIGINT UNSIGNED NOT NULL,
    responsable_id    BIGINT UNSIGNED NOT NULL,
    observaciones     VARCHAR(500) NULL,
    creado_por_id     BIGINT UNSIGNED NULL,
    creado_en         DATETIME NOT NULL,
    actualizado_en    DATETIME NOT NULL,

    CONSTRAINT fk_orden_salida_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_orden_salida_responsable FOREIGN KEY (responsable_id) REFERENCES usuario(id),
    CONSTRAINT fk_orden_salida_creado_por FOREIGN KEY (creado_por_id) REFERENCES usuario(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orden_salida_item (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    orden_salida_id         BIGINT UNSIGNED NOT NULL,
    equipo_id               BIGINT UNSIGNED NOT NULL,
    observacion_salida      VARCHAR(300) NULL,
    fecha_devolucion        DATE NULL,
    recibido_por_id         BIGINT UNSIGNED NULL,
    observacion_devolucion  VARCHAR(300) NULL,

    CONSTRAINT fk_item_orden FOREIGN KEY (orden_salida_id) REFERENCES orden_salida(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_equipo FOREIGN KEY (equipo_id) REFERENCES equipo(id),
    CONSTRAINT fk_item_recibido_por FOREIGN KEY (recibido_por_id) REFERENCES usuario(id) ON DELETE SET NULL,
    CONSTRAINT uq_orden_equipo UNIQUE (orden_salida_id, equipo_id),
    INDEX idx_item_equipo_pendiente (equipo_id, fecha_devolucion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Nuevo modulo MOVIMIENTOS en la matriz de permisos dinamicos: mismo
-- patron operativo que EQUIPOS/SERVICIOS (los 3 roles ven/crean/editan,
-- solo ADMIN elimina).
ALTER TABLE rol_permiso
    MODIFY modulo ENUM('EQUIPOS','CLIENTES','SERVICIOS','REPUESTOS','CHECKLIST','USUARIOS','RECORDATORIOS','MOVIMIENTOS') NOT NULL;

INSERT INTO rol_permiso (rol, modulo, accion, permitido) VALUES
    ('ADMIN', 'MOVIMIENTOS', 'VER', 1),
    ('ADMIN', 'MOVIMIENTOS', 'CREAR', 1),
    ('ADMIN', 'MOVIMIENTOS', 'EDITAR', 1),
    ('ADMIN', 'MOVIMIENTOS', 'ELIMINAR', 1),
    ('SUPERVISOR', 'MOVIMIENTOS', 'VER', 1),
    ('SUPERVISOR', 'MOVIMIENTOS', 'CREAR', 1),
    ('SUPERVISOR', 'MOVIMIENTOS', 'EDITAR', 1),
    ('SUPERVISOR', 'MOVIMIENTOS', 'ELIMINAR', 0),
    ('TECNICO', 'MOVIMIENTOS', 'VER', 1),
    ('TECNICO', 'MOVIMIENTOS', 'CREAR', 1),
    ('TECNICO', 'MOVIMIENTOS', 'EDITAR', 1),
    ('TECNICO', 'MOVIMIENTOS', 'ELIMINAR', 0);
