package com.inventario.dto.response;

import com.inventario.entity.AccionAuditoria;
import com.inventario.entity.Auditoria;

import java.time.LocalDateTime;

public record AuditoriaResponse(
        Long id,
        String usuarioUsername,
        String usuarioNombre,
        AccionAuditoria accion,
        String entidad,
        Long entidadId,
        String descripcion,
        LocalDateTime fecha
) {

    public static AuditoriaResponse from(Auditoria a) {
        return new AuditoriaResponse(
                a.getId(),
                a.getUsuarioUsername(),
                a.getUsuarioNombre(),
                a.getAccion(),
                a.getEntidad(),
                a.getEntidadId(),
                a.getDescripcion(),
                a.getFecha()
        );
    }
}
