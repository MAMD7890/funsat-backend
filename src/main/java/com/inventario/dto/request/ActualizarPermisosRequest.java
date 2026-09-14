package com.inventario.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ActualizarPermisosRequest(
        @NotEmpty(message = "La lista de permisos no puede estar vacía")
        @Valid
        List<PermisoItemRequest> permisos
) {
}
