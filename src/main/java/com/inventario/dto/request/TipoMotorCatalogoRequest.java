package com.inventario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoMotorCatalogoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
        String nombre,

        int orden,

        /** Opcional: en creación null = true, en edición null = mantiene el valor actual. */
        Boolean activo
) {
}
