package com.inventario.dto.request;

import com.inventario.entity.EstadoServicio;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoServicioRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoServicio estado
) {
}
