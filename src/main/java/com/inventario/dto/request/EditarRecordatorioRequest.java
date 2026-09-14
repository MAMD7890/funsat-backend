package com.inventario.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EditarRecordatorioRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar 200 caracteres")
        String titulo,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        @NotNull(message = "La fecha programada es obligatoria")
        LocalDate fechaProgramada,

        @Min(value = 1, message = "El intervalo de recurrencia debe ser de al menos 1 día")
        Integer intervaloRecurrenciaDias
) {
}
