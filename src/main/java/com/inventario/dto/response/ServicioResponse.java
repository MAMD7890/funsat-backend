package com.inventario.dto.response;

import com.inventario.entity.EstadoServicio;
import com.inventario.entity.Servicio;
import com.inventario.entity.TipoServicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ServicioResponse(
        Long id,
        String numero,
        EquipoResumenResponse equipo,
        TipoServicio tipoServicio,
        String descripcion,
        LocalDate fecha,
        BigDecimal costoValorizado,
        EstadoServicio estado,
        UsuarioResumenResponse tecnicoResponsable,
        List<ServicioRepuestoResponse> repuestos,
        List<ServicioChecklistRespuestaResponse> checklist,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {

    public static ServicioResponse from(Servicio servicio) {
        return new ServicioResponse(
                servicio.getId(),
                "SRV-" + String.format("%06d", servicio.getId()),
                EquipoResumenResponse.from(servicio.getEquipo()),
                servicio.getTipoServicio(),
                servicio.getDescripcion(),
                servicio.getFecha(),
                servicio.getCostoValorizado(),
                servicio.getEstado(),
                servicio.getTecnicoResponsable() != null
                        ? UsuarioResumenResponse.from(servicio.getTecnicoResponsable())
                        : null,
                servicio.getRepuestos().stream().map(ServicioRepuestoResponse::from).toList(),
                servicio.getChecklist().stream().map(ServicioChecklistRespuestaResponse::from).toList(),
                servicio.getCreadoEn(),
                servicio.getActualizadoEn()
        );
    }
}
