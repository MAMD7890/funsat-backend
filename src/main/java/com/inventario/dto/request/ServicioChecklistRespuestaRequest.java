package com.inventario.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServicioChecklistRespuestaRequest(
        @NotNull(message = "El ítem de checklist es obligatorio")
        Long itemId,

        boolean completado,

        @Size(max = 300, message = "La observación no puede superar 300 caracteres")
        String observacion
) {
}
