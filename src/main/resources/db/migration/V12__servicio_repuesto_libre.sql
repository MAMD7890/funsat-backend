-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  "Repuestos utilizados" dentro de un Servicio deja de exigir un
--  catalogo Repuesto pre-creado: pasa a ser nombre + costo unitario
--  libres, cargados fila por fila directamente en el formulario.
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V12
-- =============================================================

ALTER TABLE servicio_repuesto
    ADD COLUMN nombre         VARCHAR(160)  NULL AFTER repuesto_id,
    ADD COLUMN costo_unitario DECIMAL(14,2) NULL AFTER nombre;

-- Conserva el nombre y precio del repuesto tal como estaban en el
-- catalogo al momento de esta migracion, para no perder el historial
-- de servicios ya registrados.
UPDATE servicio_repuesto sr
    JOIN repuesto r ON sr.repuesto_id = r.id
    SET sr.nombre = r.nombre,
        sr.costo_unitario = r.costo_unitario;

ALTER TABLE servicio_repuesto
    MODIFY COLUMN nombre         VARCHAR(160)  NOT NULL,
    MODIFY COLUMN costo_unitario DECIMAL(14,2) NOT NULL;

-- uq_servicio_repuesto (servicio_id, repuesto_id) es hoy el unico indice que
-- soporta la FK fk_servrep_servicio (servicio_id es su columna izquierda);
-- hay que darle un indice propio antes de poder soltar ese UNIQUE.
ALTER TABLE servicio_repuesto
    ADD INDEX idx_servrep_servicio (servicio_id);

ALTER TABLE servicio_repuesto
    DROP FOREIGN KEY fk_servrep_repuesto,
    DROP INDEX fk_servrep_repuesto,
    DROP INDEX uq_servicio_repuesto,
    DROP COLUMN repuesto_id;
