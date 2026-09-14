package com.inventario.dto.response;

import com.inventario.entity.EstadoRecordatorio;
import com.inventario.entity.RecordatorioMantenimiento;

import java.time.LocalDate;

public record RecordatorioResponse(
        Long id,
        Long equipoId,
        String equipoDescripcion,
        String equipoCodigo,
        String titulo,
        String descripcion,
        LocalDate fechaProgramada,
        EstadoRecordatorio estado,
        boolean vencido,
        Integer intervaloRecurrenciaDias,
        String creadoPorNombre
) {

    public static RecordatorioResponse from(RecordatorioMantenimiento r) {
        return new RecordatorioResponse(
                r.getId(),
                r.getEquipo().getId(),
                r.getEquipo().getDescripcionEquipo(),
                r.getEquipo().getCodigo() != null ? r.getEquipo().getCodigo() : r.getEquipo().getCodigoTaller(),
                r.getTitulo(),
                r.getDescripcion(),
                r.getFechaProgramada(),
                r.getEstado(),
                r.isVencido(),
                r.getIntervaloRecurrenciaDias(),
                r.getCreadoPor() != null ? r.getCreadoPor().getNombre() : null
        );
    }
}
