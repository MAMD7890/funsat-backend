package com.inventario.dto.response;

import com.inventario.entity.OrdenSalidaItemAccesorio;

import java.time.LocalDate;

public record OrdenSalidaItemAccesorioResponse(
        Long id,
        Long accesorioId,
        String nombre,
        int cantidad,
        boolean devuelto,
        LocalDate fechaDevolucion,
        UsuarioResumenResponse recibidoPor,
        String observacionDevolucion
) {

    public static OrdenSalidaItemAccesorioResponse from(OrdenSalidaItemAccesorio item) {
        return new OrdenSalidaItemAccesorioResponse(
                item.getId(),
                item.getAccesorio().getId(),
                item.getAccesorio().getNombre(),
                item.getAccesorio().getCantidad(),
                item.isDevuelto(),
                item.getFechaDevolucion(),
                item.getRecibidoPor() != null ? UsuarioResumenResponse.from(item.getRecibidoPor()) : null,
                item.getObservacionDevolucion()
        );
    }
}
