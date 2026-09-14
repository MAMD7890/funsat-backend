package com.inventario.dto.response;

import com.inventario.entity.ServicioRepuesto;

import java.math.BigDecimal;

public record ServicioRepuestoResponse(
        Long id,
        String nombre,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        BigDecimal costoTotal
) {

    public static ServicioRepuestoResponse from(ServicioRepuesto servicioRepuesto) {
        BigDecimal costoUnitario = servicioRepuesto.getCostoUnitario();
        BigDecimal cantidad = servicioRepuesto.getCantidad();
        return new ServicioRepuestoResponse(
                servicioRepuesto.getId(),
                servicioRepuesto.getNombre(),
                cantidad,
                costoUnitario,
                costoUnitario.multiply(cantidad)
        );
    }
}
