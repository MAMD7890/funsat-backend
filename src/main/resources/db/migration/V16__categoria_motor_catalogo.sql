-- =============================================================
--  SISTEMA FUNSAT
--  Convierte Categoria y TipoMotor de enums Java fijos a catalogos
--  editables por el administrador (tablas nuevas + FK en equipo y
--  checklist_item_catalogo). Los valores existentes se preservan
--  migrando cada fila al catalogo equivalente.
-- =============================================================

CREATE TABLE categoria_catalogo (
    id                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre                 VARCHAR(80) NOT NULL,
    requiere_numero_serie  BOOLEAN NOT NULL DEFAULT FALSE,
    orden                  INT NOT NULL DEFAULT 0,
    activo                 BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_categoria_catalogo_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tipo_motor_catalogo (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(80) NOT NULL,
    orden      INT NOT NULL DEFAULT 0,
    activo     BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tipo_motor_catalogo_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categoria_catalogo (nombre, requiere_numero_serie, orden) VALUES
    ('Equipo Menor', FALSE, 1),
    ('Equipo Mayor', FALSE, 2),
    ('Vehículo', TRUE, 3),
    ('Tráiler', TRUE, 4),
    ('Maquinaria', FALSE, 5);

INSERT INTO tipo_motor_catalogo (nombre, orden) VALUES
    ('Gasolina 2T', 1),
    ('Gasolina 4T', 2),
    ('Diésel', 3),
    ('Eléctrico 110V', 4),
    ('Eléctrico 220V', 5),
    ('Hidráulico', 6),
    ('Sin motor', 7);

-- ---------- equipo.categoria (String enum) -> equipo.categoria_id (FK) ----------

ALTER TABLE equipo ADD COLUMN categoria_id BIGINT UNSIGNED NULL AFTER categoria;

UPDATE equipo e
JOIN categoria_catalogo c ON c.nombre = CASE e.categoria
    WHEN 'EQUIPO_MENOR' THEN 'Equipo Menor'
    WHEN 'EQUIPO_MAYOR' THEN 'Equipo Mayor'
    WHEN 'VEHICULO' THEN 'Vehículo'
    WHEN 'TRAILER' THEN 'Tráiler'
    WHEN 'MAQUINARIA' THEN 'Maquinaria'
END
SET e.categoria_id = c.id;

ALTER TABLE equipo MODIFY COLUMN categoria_id BIGINT UNSIGNED NOT NULL;
ALTER TABLE equipo ADD CONSTRAINT fk_equipo_categoria FOREIGN KEY (categoria_id) REFERENCES categoria_catalogo(id);
ALTER TABLE equipo DROP COLUMN categoria;
CREATE INDEX idx_equipo_categoria ON equipo(categoria_id);

-- ---------- equipo.motor (String enum, nullable) -> equipo.motor_id (FK, nullable) ----------

ALTER TABLE equipo ADD COLUMN motor_id BIGINT UNSIGNED NULL AFTER motor;

UPDATE equipo e
JOIN tipo_motor_catalogo m ON m.nombre = CASE e.motor
    WHEN 'GASOLINA_2T' THEN 'Gasolina 2T'
    WHEN 'GASOLINA_4T' THEN 'Gasolina 4T'
    WHEN 'DIESEL' THEN 'Diésel'
    WHEN 'ELECTRICO_110V' THEN 'Eléctrico 110V'
    WHEN 'ELECTRICO_220V' THEN 'Eléctrico 220V'
    WHEN 'HIDRAULICO' THEN 'Hidráulico'
    WHEN 'SIN_MOTOR' THEN 'Sin motor'
END
SET e.motor_id = m.id
WHERE e.motor IS NOT NULL;

ALTER TABLE equipo ADD CONSTRAINT fk_equipo_motor FOREIGN KEY (motor_id) REFERENCES tipo_motor_catalogo(id);
ALTER TABLE equipo DROP COLUMN motor;
CREATE INDEX idx_equipo_motor ON equipo(motor_id);

-- ---------- checklist_item_catalogo.categoria (String enum) -> categoria_id (FK) ----------

ALTER TABLE checklist_item_catalogo ADD COLUMN categoria_id BIGINT UNSIGNED NULL AFTER categoria;

UPDATE checklist_item_catalogo t
JOIN categoria_catalogo c ON c.nombre = CASE t.categoria
    WHEN 'EQUIPO_MENOR' THEN 'Equipo Menor'
    WHEN 'EQUIPO_MAYOR' THEN 'Equipo Mayor'
    WHEN 'VEHICULO' THEN 'Vehículo'
    WHEN 'TRAILER' THEN 'Tráiler'
    WHEN 'MAQUINARIA' THEN 'Maquinaria'
END
SET t.categoria_id = c.id;

ALTER TABLE checklist_item_catalogo MODIFY COLUMN categoria_id BIGINT UNSIGNED NOT NULL;
ALTER TABLE checklist_item_catalogo ADD CONSTRAINT fk_checklist_item_categoria
    FOREIGN KEY (categoria_id) REFERENCES categoria_catalogo(id);
ALTER TABLE checklist_item_catalogo DROP COLUMN categoria;
CREATE INDEX idx_checklist_item_categoria ON checklist_item_catalogo(categoria_id);
