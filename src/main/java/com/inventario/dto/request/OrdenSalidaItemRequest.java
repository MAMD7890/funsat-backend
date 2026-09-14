package com.inventario.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrdenSalidaItemRequest(
        @NotNull(message = "El equipo es obligatorio")
        Long equipoId,

        @Size(max = 300, message = "La observación no puede superar 300 caracteres")
        String observacionSalida,

        List<Long> accesorioIds
) {

    public OrdenSalidaItemRequest {
        if (accesorioIds == null) {
            accesorioIds = List.of();
        }
    }
}
