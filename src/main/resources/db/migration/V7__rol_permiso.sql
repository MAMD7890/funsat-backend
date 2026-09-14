-- =============================================================
--  SISTEMA DE INVENTARIO GENERAL DE EQUIPOS
--  Modulo: Roles y Permisos dinamicos
--  Motor: MySQL 8.0+  |  Charset: utf8mb4  |  Flyway: V7__rol_permiso.sql
--
--  El seed reproduce EXACTAMENTE el comportamiento que ya estaba
--  hardcoded en los controllers (los 3 roles crean/editan/ven,
--  solo ADMIN elimina) para que activar este modulo no cambie nada
--  hasta que un ADMIN edite la matriz desde /roles.
--
--  Importacion y la gestion de este mismo catalogo de roles quedan
--  fuera a proposito (siguen hardcoded a ADMIN en el codigo) para
--  que un ADMIN nunca pueda quedar bloqueado fuera de /roles.
-- =============================================================

CREATE TABLE rol_permiso (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rol        ENUM('ADMIN','SUPERVISOR','TECNICO') NOT NULL,
    modulo     ENUM('EQUIPOS','CLIENTES','SERVICIOS','REPUESTOS','CHECKLIST','USUARIOS') NOT NULL,
    accion     ENUM('VER','CREAR','EDITAR','ELIMINAR') NOT NULL,
    permitido  TINYINT(1) NOT NULL DEFAULT 0,

    CONSTRAINT uq_rol_permiso UNIQUE (rol, modulo, accion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ADMIN: acceso total a todo
INSERT INTO rol_permiso (rol, modulo, accion, permitido)
SELECT 'ADMIN', m.modulo, a.accion, 1
FROM (SELECT 'EQUIPOS' AS modulo UNION SELECT 'CLIENTES' UNION SELECT 'SERVICIOS'
      UNION SELECT 'REPUESTOS' UNION SELECT 'CHECKLIST' UNION SELECT 'USUARIOS') m
CROSS JOIN (SELECT 'VER' AS accion UNION SELECT 'CREAR' UNION SELECT 'EDITAR' UNION SELECT 'ELIMINAR') a;

-- SUPERVISOR y TECNICO: ver/crear/editar en los modulos operativos, nunca eliminar
INSERT INTO rol_permiso (rol, modulo, accion, permitido)
SELECT r.rol, m.modulo, a.accion, 1
FROM (SELECT 'SUPERVISOR' AS rol UNION SELECT 'TECNICO') r
CROSS JOIN (SELECT 'EQUIPOS' AS modulo UNION SELECT 'CLIENTES' UNION SELECT 'SERVICIOS'
            UNION SELECT 'REPUESTOS' UNION SELECT 'CHECKLIST') m
CROSS JOIN (SELECT 'VER' AS accion UNION SELECT 'CREAR' UNION SELECT 'EDITAR') a;

INSERT INTO rol_permiso (rol, modulo, accion, permitido)
SELECT r.rol, m.modulo, 'ELIMINAR', 0
FROM (SELECT 'SUPERVISOR' AS rol UNION SELECT 'TECNICO') r
CROSS JOIN (SELECT 'EQUIPOS' AS modulo UNION SELECT 'CLIENTES' UNION SELECT 'SERVICIOS'
            UNION SELECT 'REPUESTOS' UNION SELECT 'CHECKLIST') m;

-- USUARIOS: GET /usuarios es de lectura para cualquier autenticado (VER=1
-- para los 3 roles); crear un usuario (POST /auth/register) solo ADMIN;
-- no existen endpoints de editar/eliminar usuario todavia (permitido=0,
-- sin efecto real hasta que existan).
INSERT INTO rol_permiso (rol, modulo, accion, permitido) VALUES
    ('SUPERVISOR', 'USUARIOS', 'VER', 1),
    ('SUPERVISOR', 'USUARIOS', 'CREAR', 0),
    ('SUPERVISOR', 'USUARIOS', 'EDITAR', 0),
    ('SUPERVISOR', 'USUARIOS', 'ELIMINAR', 0),
    ('TECNICO', 'USUARIOS', 'VER', 1),
    ('TECNICO', 'USUARIOS', 'CREAR', 0),
    ('TECNICO', 'USUARIOS', 'EDITAR', 0),
    ('TECNICO', 'USUARIOS', 'ELIMINAR', 0);
