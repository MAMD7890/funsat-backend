-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Estado de Servicio (tablero Kanban)
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V9__servicio_estado.sql
--
--  Los servicios ya existentes quedan en COMPLETADO por defecto: se
--  crearon con costo, checklist y repuestos ya registrados, el perfil de
--  un trabajo ya cerrado, no de uno pendiente.
-- =============================================================

ALTER TABLE servicio
    ADD COLUMN estado ENUM('REGISTRADO','EN_PROGRESO','EN_REVISION','COMPLETADO')
        NOT NULL DEFAULT 'COMPLETADO';
