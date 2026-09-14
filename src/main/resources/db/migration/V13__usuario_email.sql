-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Correo del usuario, opcional: destino de las alertas por email
--  (resumen diario de mantenimientos vencidos/proximos y equipos
--  que llevan demasiado tiempo en la calle).
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V13
-- =============================================================

ALTER TABLE usuario
    ADD COLUMN email VARCHAR(180) NULL AFTER username;
