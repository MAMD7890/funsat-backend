package com.inventario.dto.response;

import com.inventario.entity.TipoMotorCatalogo;

public record TipoMotorCatalogoResponse(
        Long id,
        String nombre,
        int orden,
        boolean activo
) {

    public static TipoMotorCatalogoResponse from(TipoMotorCatalogo motor) {
        return new TipoMotorCatalogoResponse(
                motor.getId(),
                motor.getNombre(),
                motor.getOrden(),
                motor.isActivo()
        );
    }
}
