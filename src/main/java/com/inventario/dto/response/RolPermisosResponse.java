package com.inventario.dto.response;

import com.inventario.entity.Rol;

import java.util.List;

public record RolPermisosResponse(
        Rol rol,
        long usuariosActivos,
        List<PermisoResponse> permisos
) {
}
