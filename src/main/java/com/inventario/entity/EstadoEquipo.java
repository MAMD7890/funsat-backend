package com.inventario.entity;

/**
 * Estado fisico/operativo general del equipo. Independiente de
 * {@link EstadoServicioTaller}, que describe el flujo de servicio dentro
 * del taller (a proposito no se mezclan en un solo enum).
 */
public enum EstadoEquipo {
    ACTIVO,
    INACTIVO,
    EN_MANTENIMIENTO,
    DADO_DE_BAJA
}
