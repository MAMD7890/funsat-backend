package com.inventario.dto.response;

import com.inventario.entity.EquipoAccesorio;

public record EquipoAccesorioResponse(
        Long id,
        String nombre,
        int cantidad,
        String ubicacionActual,
        boolean enCalle
) {

    public static EquipoAccesorioResponse from(EquipoAccesorio accesorio) {
        return new EquipoAccesorioResponse(accesorio.getId(), accesorio.getNombre(), accesorio.getCantidad(), null, false);
    }

    public static EquipoAccesorioResponse from(EquipoAccesorio accesorio, String ubicacionActual, boolean enCalle) {
        return new EquipoAccesorioResponse(accesorio.getId(), accesorio.getNombre(), accesorio.getCantidad(), ubicacionActual, enCalle);
    }
}
