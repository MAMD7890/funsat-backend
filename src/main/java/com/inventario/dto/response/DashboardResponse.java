package com.inventario.dto.response;

import com.inventario.entity.EstadoServicio;
import com.inventario.entity.Propiedad;
import com.inventario.entity.TipoServicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Resumen agregado para el Dashboard: estado del parque de equipos (incluida su ubicación) y de los mantenimientos. */
public record DashboardResponse(
        Equipos equipos,
        Servicios servicios,
        Mantenimientos mantenimientos,
        EquiposEnCalleResumen equiposEnCalleResumen,
        List<ServicioReciente> serviciosRecientes,
        List<EquipoEnCalle> equiposEnCalle
) {

    public record Equipos(
            long total,
            long activos,
            long inactivos,
            long enMantenimiento,
            long dadosDeBaja,
            long enAlmacen,
            long enCalle,
            List<CategoriaConteo> porCategoria,
            Map<Propiedad, Long> porPropiedad
    ) {
    }

    public record CategoriaConteo(Long categoriaId, String categoriaNombre, long cantidad) {
    }

    public record Servicios(
            long total,
            Map<EstadoServicio, Long> porEstado,
            Map<TipoServicio, Long> porTipo,
            BigDecimal costoTotalHistorico,
            BigDecimal costoMesActual
    ) {
    }

    public record Mantenimientos(
            long vencidos,
            long proximos7Dias
    ) {
    }

    /** diasFueraAlerta: umbral (app.alertas.dias-equipo-en-calle) usado para marcar equipos con salida prolongada. */
    public record EquiposEnCalleResumen(
            long total,
            long enAlerta,
            int diasFueraAlerta
    ) {
    }

    public record ServicioReciente(
            Long id,
            String numero,
            String equipoDescripcion,
            TipoServicio tipoServicio,
            EstadoServicio estado,
            LocalDate fecha,
            String tecnicoNombre
    ) {
    }

    public record EquipoEnCalle(
            Long equipoId,
            String equipoDescripcion,
            String equipoCodigo,
            String clienteNombre,
            LocalDate fechaSalida,
            long diasFuera,
            boolean enAlerta
    ) {
    }
}
