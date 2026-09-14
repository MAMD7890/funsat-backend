package com.inventario.entity;

/**
 * Estado del flujo de servicio dentro del taller (diagnostico, aprobacion,
 * reparacion, entrega). Independiente de {@link EstadoEquipo}, que describe
 * el estado fisico/operativo general del equipo.
 */
public enum EstadoServicioTaller {
    PENDIENTE_POR_DIAGNOSTICO,
    POR_INICIAR_SERVICIO,
    POR_APROBAR,
    POR_DEFINIR,
    STAND_BY,
    SIN_CANCELAR_Y_REPARADO,
    PENDIENTE_REMISION,
    PENDIENTE_POR_FACTURA_Y_ENTREGA,
    REPARADA_POR_RETIRAR
}
