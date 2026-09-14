package com.inventario.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipoAccesorioRequest(
        Long id,

        @NotBlank(message = "El nombre del accesorio es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad
) {
}
