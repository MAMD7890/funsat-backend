package com.inventario.entity;

/**
 * Modulos del sistema cuyos permisos son configurables por rol via
 * RolPermiso. Importacion y la gestion de este mismo catalogo de roles
 * quedan a proposito fuera (hardcoded a ADMIN) para que un ADMIN nunca
 * pueda quedar bloqueado fuera de /roles por un error en la matriz.
 */
public enum Modulo {
    EQUIPOS,
    CLIENTES,
    SERVICIOS,
    REPUESTOS,
    CHECKLIST,
    USUARIOS,
    RECORDATORIOS,
    MOVIMIENTOS
}
