package com.inventario.entity;

/**
 * Progreso del servicio en el tablero Kanban. Independiente de
 * {@link EstadoServicioTaller} (que describe el flujo de un equipo EXTERNO
 * dentro del taller) y de {@link EstadoEquipo} (estado operativo del
 * equipo). Los servicios existentes antes de este campo se consideran
 * COMPLETADO por defecto (ya traen costo, checklist y repuestos
 * registrados, el perfil de un trabajo ya cerrado).
 */
public enum EstadoServicio {
    REGISTRADO,
    EN_PROGRESO,
    EN_REVISION,
    COMPLETADO
}
