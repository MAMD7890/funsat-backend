-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Recordatorios de Mantenimiento (Calendario)
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V8__recordatorio_mantenimiento.sql
--
--  Recordatorios programados manualmente por equipo, independientes del
--  flujo de Servicios (completar un recordatorio solo cambia su propio
--  estado; no crea ni exige un Servicio). Soportan recurrencia simple:
--  al completar uno con intervalo_recurrencia_dias, se crea otro
--  automaticamente con fecha_programada + ese intervalo.
-- =============================================================

CREATE TABLE recordatorio_mantenimiento (
    id                          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    equipo_id                   BIGINT UNSIGNED NOT NULL,
    titulo                      VARCHAR(200) NOT NULL,
    descripcion                 VARCHAR(500) NULL,
    fecha_programada            DATE NOT NULL,
    estado                      ENUM('PENDIENTE','COMPLETADO') NOT NULL DEFAULT 'PENDIENTE',
    intervalo_recurrencia_dias  INT UNSIGNED NULL,
    creado_por_id               BIGINT UNSIGNED NULL,
    creado_en                   DATETIME NOT NULL,
    actualizado_en              DATETIME NOT NULL,

    CONSTRAINT fk_recordatorio_equipo FOREIGN KEY (equipo_id) REFERENCES equipo(id),
    CONSTRAINT fk_recordatorio_usuario FOREIGN KEY (creado_por_id) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_recordatorio_fecha (fecha_programada),
    INDEX idx_recordatorio_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Nuevo modulo RECORDATORIOS en la matriz de permisos dinamicos: mismo
-- patron que los modulos operativos (EQUIPOS/SERVICIOS/etc): los 3 roles
-- ven/crean/editan, solo ADMIN elimina.
ALTER TABLE rol_permiso
    MODIFY modulo ENUM('EQUIPOS','CLIENTES','SERVICIOS','REPUESTOS','CHECKLIST','USUARIOS','RECORDATORIOS') NOT NULL;

INSERT INTO rol_permiso (rol, modulo, accion, permitido) VALUES
    ('ADMIN', 'RECORDATORIOS', 'VER', 1),
    ('ADMIN', 'RECORDATORIOS', 'CREAR', 1),
    ('ADMIN', 'RECORDATORIOS', 'EDITAR', 1),
    ('ADMIN', 'RECORDATORIOS', 'ELIMINAR', 1),
    ('SUPERVISOR', 'RECORDATORIOS', 'VER', 1),
    ('SUPERVISOR', 'RECORDATORIOS', 'CREAR', 1),
    ('SUPERVISOR', 'RECORDATORIOS', 'EDITAR', 1),
    ('SUPERVISOR', 'RECORDATORIOS', 'ELIMINAR', 0),
    ('TECNICO', 'RECORDATORIOS', 'VER', 1),
    ('TECNICO', 'RECORDATORIOS', 'CREAR', 1),
    ('TECNICO', 'RECORDATORIOS', 'EDITAR', 1),
    ('TECNICO', 'RECORDATORIOS', 'ELIMINAR', 0);
