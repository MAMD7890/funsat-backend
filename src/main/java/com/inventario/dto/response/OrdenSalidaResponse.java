package com.inventario.dto.response;

import com.inventario.entity.OrdenSalida;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenSalidaResponse(
        Long id,
        String numero,
        LocalDate fechaSalida,
        ClienteResponse cliente,
        UsuarioResumenResponse responsable,
        String observaciones,
        String estado,
        List<OrdenSalidaItemResponse> items,
        String creadoPorNombre,
        LocalDateTime creadoEn
) {

    public static OrdenSalidaResponse from(OrdenSalida orden) {
        List<OrdenSalidaItemResponse> items = orden.getItems().stream()
                .map(OrdenSalidaItemResponse::from)
                .toList();

        long devueltos = items.stream().filter(OrdenSalidaItemResponse::devuelto).count();
        String estado;
        if (devueltos == 0) {
            estado = "ABIERTA";
        } else if (devueltos == items.size()) {
            estado = "CERRADA";
        } else {
            estado = "PARCIAL";
        }

        return new OrdenSalidaResponse(
                orden.getId(),
                "OS-" + String.format("%06d", orden.getId()),
                orden.getFechaSalida(),
                ClienteResponse.from(orden.getCliente()),
                UsuarioResumenResponse.from(orden.getResponsable()),
                orden.getObservaciones(),
                estado,
                items,
                orden.getCreadoPor() != null ? orden.getCreadoPor().getNombre() : null,
                orden.getCreadoEn()
        );
    }
}
