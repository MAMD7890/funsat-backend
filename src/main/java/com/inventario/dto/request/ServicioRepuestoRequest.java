package com.inventario.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicioRepuestoRequest(
        Long id,

        @NotBlank(message = "El nombre del repuesto es obligatorio")
        @Size(max = 160, message = "El nombre no puede superar 160 caracteres")
        String nombre,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal cantidad,

        @NotNull(message = "El costo unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El costo unitario no puede ser negativo")
        BigDecimal costoUnitario
) {
}
