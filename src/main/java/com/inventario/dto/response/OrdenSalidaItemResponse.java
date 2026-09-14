package com.inventario.dto.response;

import com.inventario.entity.OrdenSalidaItem;

import java.time.LocalDate;
import java.util.List;

public record OrdenSalidaItemResponse(
        Long id,
        Long equipoId,
        String equipoDescripcion,
        String equipoCodigo,
        String equipoMarca,
        String equipoNumeroSerie,
        String observacionSalida,
        List<OrdenSalidaItemAccesorioResponse> accesorios,
        boolean devuelto,
        LocalDate fechaDevolucion,
        UsuarioResumenResponse recibidoPor,
        String observacionDevolucion
) {

    public static OrdenSalidaItemResponse from(OrdenSalidaItem item) {
        return new OrdenSalidaItemResponse(
                item.getId(),
                item.getEquipo().getId(),
                item.getEquipo().getDescripcionEquipo(),
                item.getEquipo().getCodigo() != null ? item.getEquipo().getCodigo() : item.getEquipo().getCodigoTaller(),
                item.getEquipo().getMarca(),
                item.getEquipo().getNumeroSerie(),
                item.getObservacionSalida(),
                item.getAccesorios().stream().map(OrdenSalidaItemAccesorioResponse::from).toList(),
                item.isDevuelto(),
                item.getFechaDevolucion(),
                item.getRecibidoPor() != null ? UsuarioResumenResponse.from(item.getRecibidoPor()) : null,
                item.getObservacionDevolucion()
        );
    }
}
