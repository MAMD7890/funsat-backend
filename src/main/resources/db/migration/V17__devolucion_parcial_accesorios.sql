-- =============================================================
--  SISTEMA FUNSAT
--  Devolucion parcial de accesorios: orden_salida_item_accesorio pasa de
--  ser un simple cruce (equipo/accesorio salio con esta orden si o si) a
--  llevar su propio estado de devolucion, independiente del equipo
--  principal. Asi se puede registrar la entrada del equipo aunque uno de
--  sus accesorios se quede pendiente, y la ubicacion de ese accesorio
--  sigue mostrando el cliente hasta que se registre su propia entrada.
-- =============================================================

-- Paso 1: se agrega el UNIQUE KEY (mismas columnas que la PRIMARY KEY
-- compuesta actual) ANTES de tocar la PK. Con esto fk_item_accesorio_item
-- ya tiene un indice alternativo (la columna lider de este UNIQUE KEY) para
-- cuando se suelte la PK en el paso 2 -si se intenta todo en un solo ALTER,
-- o se suelta la PK antes de crear el reemplazo, falla con error 1553
-- ("Cannot drop index: needed in a foreign key constraint")-.
ALTER TABLE orden_salida_item_accesorio
    ADD UNIQUE KEY uq_item_accesorio (orden_salida_item_id, equipo_accesorio_id);

-- Paso 2: ahora es seguro reemplazar la PK compuesta por un id propio
-- (fk_item_accesorio_item queda respaldado por uq_item_accesorio,
-- fk_item_accesorio_accesorio ya tenia su propio indice desde el inicio).
ALTER TABLE orden_salida_item_accesorio
    DROP PRIMARY KEY,
    ADD COLUMN id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD COLUMN fecha_devolucion DATE NULL,
    ADD COLUMN recibido_por_id BIGINT UNSIGNED NULL,
    ADD COLUMN observacion_devolucion VARCHAR(300) NULL,
    ADD CONSTRAINT fk_item_accesorio_recibido_por FOREIGN KEY (recibido_por_id) REFERENCES usuario(id) ON DELETE SET NULL;

-- Los accesorios de items ya devueltos antes de esta migracion se dan por
-- devueltos junto con su equipo (mismo comportamiento que tenian hasta ahora).
UPDATE orden_salida_item_accesorio oia
JOIN orden_salida_item oi ON oi.id = oia.orden_salida_item_id
SET oia.fecha_devolucion = oi.fecha_devolucion,
    oia.recibido_por_id = oi.recibido_por_id
WHERE oi.fecha_devolucion IS NOT NULL;

CREATE INDEX idx_item_accesorio_pendiente ON orden_salida_item_accesorio(equipo_accesorio_id, fecha_devolucion);
