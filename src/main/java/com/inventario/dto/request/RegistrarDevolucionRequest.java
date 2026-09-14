package com.inventario.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record RegistrarDevolucionRequest(
        @NotNull(message = "La fecha de devolución es obligatoria")
        LocalDate fechaDevolucion,

        @NotNull(message = "Quién recibe el equipo es obligatorio")
        Long recibidoPorId,

        @Size(max = 300, message = "La observación no puede superar 300 caracteres")
        String observacionDevolucion,

        /**
         * Solo aplica al registrar la entrada del equipo (no a la devolución
         * individual de un accesorio pendiente): ids de EquipoAccesorio que
         * vuelven junto con el equipo en este momento. Los que no estén en
         * la lista quedan pendientes (el equipo puede volver sin todos sus
         * accesorios).
         */
        List<Long> accesorioIdsDevueltos
) {

    public RegistrarDevolucionRequest {
        if (accesorioIdsDevueltos == null) {
            accesorioIdsDevueltos = List.of();
        }
    }
}
