package com.inventario.dto.request;

import com.inventario.entity.EstadoServicio;
import com.inventario.entity.TipoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServicioRequest(
        @NotNull(message = "El equipo es obligatorio")
        Long equipoId,

        @NotNull(message = "El tipo de servicio es obligatorio")
        TipoServicio tipoServicio,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @NotNull(message = "El costo valorizado es obligatorio")
        @DecimalMin(value = "0.0", message = "El costo valorizado no puede ser negativo")
        BigDecimal costoValorizado,

        @NotNull(message = "El técnico responsable es obligatorio")
        Long tecnicoResponsableId,

        /** Opcional: si se omite, un servicio nuevo queda REGISTRADO ("Por hacer") y uno existente no cambia de estado. */
        EstadoServicio estado,

        @Valid
        List<ServicioRepuestoRequest> repuestos,

        @Valid
        List<ServicioChecklistRespuestaRequest> checklist
) {

    public ServicioRequest {
        if (repuestos == null) {
            repuestos = List.of();
        }
        if (checklist == null) {
            checklist = List.of();
        }
    }
}
