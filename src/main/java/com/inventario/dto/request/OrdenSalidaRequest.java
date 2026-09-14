package com.inventario.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record OrdenSalidaRequest(
        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotNull(message = "El cliente/destino es obligatorio")
        Long clienteId,

        @NotNull(message = "El responsable es obligatorio")
        Long responsableId,

        @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
        String observaciones,

        @NotEmpty(message = "La orden debe incluir al menos un equipo")
        @Valid
        List<OrdenSalidaItemRequest> items
) {
}
