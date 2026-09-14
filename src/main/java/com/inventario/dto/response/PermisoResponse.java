package com.inventario.dto.response;

import com.inventario.entity.Accion;
import com.inventario.entity.Modulo;
import com.inventario.entity.RolPermiso;

public record PermisoResponse(
        Modulo modulo,
        Accion accion,
        boolean permitido
) {

    public static PermisoResponse from(RolPermiso rolPermiso) {
        return new PermisoResponse(rolPermiso.getModulo(), rolPermiso.getAccion(), rolPermiso.isPermitido());
    }
}
