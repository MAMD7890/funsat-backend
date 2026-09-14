package com.inventario.dto.response;

import com.inventario.entity.ServicioChecklistRespuesta;

public record ServicioChecklistRespuestaResponse(
        Long itemId,
        String nombreItem,
        boolean completado,
        String observacion
) {

    public static ServicioChecklistRespuestaResponse from(ServicioChecklistRespuesta respuesta) {
        return new ServicioChecklistRespuestaResponse(
                respuesta.getItem().getId(),
                respuesta.getItem().getNombre(),
                respuesta.isCompletado(),
                respuesta.getObservacion()
        );
    }
}
