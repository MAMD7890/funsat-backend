package com.inventario.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RepuestoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 160, message = "El nombre no puede superar 160 caracteres")
        String nombre,

        @Size(max = 60, message = "El código no puede superar 60 caracteres")
        String codigo,

        @NotNull(message = "El costo unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El costo unitario no puede ser negativo")
        BigDecimal costoUnitario,

        @NotNull(message = "El stock disponible es obligatorio")
        @DecimalMin(value = "0.0", message = "El stock disponible no puede ser negativo")
        BigDecimal stockDisponible
) {
}
