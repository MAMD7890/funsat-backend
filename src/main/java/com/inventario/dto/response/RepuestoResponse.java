package com.inventario.dto.response;

import com.inventario.entity.Repuesto;

import java.math.BigDecimal;

public record RepuestoResponse(
        Long id,
        String nombre,
        String codigo,
        BigDecimal costoUnitario,
        BigDecimal stockDisponible
) {

    public static RepuestoResponse from(Repuesto repuesto) {
        return new RepuestoResponse(
                repuesto.getId(),
                repuesto.getNombre(),
                repuesto.getCodigo(),
                repuesto.getCostoUnitario(),
                repuesto.getStockDisponible()
        );
    }
}
