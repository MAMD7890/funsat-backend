package com.inventario.dto.request;

import com.inventario.entity.EstadoEquipo;
import com.inventario.entity.EstadoServicioTaller;
import com.inventario.entity.Propiedad;
import com.inventario.validation.ValidEquipoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Un solo DTO para equipos propios y externos: las reglas condicionales
 * (numero de serie segun categoria, cliente/fecha de ingreso obligatorios
 * solo si es EXTERNO) se validan en {@link ValidEquipoRequest}.
 */
@ValidEquipoRequest
public record EquipoRequest(
        @Size(max = 30, message = "El código no puede superar 30 caracteres")
        String codigo,

        // "^$|..." porque @Pattern, a diferencia de @Email, NO trata "" como valido
        // (solo null pasa gratis) — sin el "^$|" un codigoTaller vacio rechazaba la
        // creacion de cualquier equipo que no lo llenara, que es la mayoria.
        @Pattern(
                regexp = "^$|^\\d{2}-[A-Z]{1,6}-\\d{2,}$",
                message = "El código de taller debe tener el formato AA-XX-NN (ej. 26-MS-01)"
        )
        String codigoTaller,

        @NotBlank(message = "La descripción del equipo es obligatoria")
        @Size(max = 200, message = "La descripción no puede superar 200 caracteres")
        String descripcionEquipo,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @Size(max = 100)
        String marca,

        String numeroSerie,

        @Size(max = 100)
        String modelo,

        Long motorId,

        @Size(max = 160)
        String ubicacion,

        EstadoEquipo estado,

        boolean checkMtto,
        boolean checkHv,
        boolean checkFt,

        @NotNull(message = "La propiedad (PROPIO/EXTERNO) es obligatoria")
        Propiedad propiedad,

        Long clienteId,

        String accesorios,

        LocalDate fechaIngreso,
        LocalDate fechaDiagnostico,

        boolean rotulado,

        EstadoServicioTaller estadoServicioTaller,

        @Valid
        List<EquipoAccesorioRequest> accesoriosRegistrados
) {

    public EquipoRequest {
        if (accesoriosRegistrados == null) {
            accesoriosRegistrados = List.of();
        }
    }
}
