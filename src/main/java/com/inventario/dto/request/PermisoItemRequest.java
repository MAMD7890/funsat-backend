package com.inventario.dto.request;

import com.inventario.entity.Accion;
import com.inventario.entity.Modulo;
import jakarta.validation.constraints.NotNull;

public record PermisoItemRequest(
        @NotNull(message = "El módulo es obligatorio") Modulo modulo,
        @NotNull(message = "La acción es obligatoria") Accion accion,
        boolean permitido
) {
}
