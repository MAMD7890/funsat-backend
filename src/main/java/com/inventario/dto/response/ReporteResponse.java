package com.inventario.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteResponse(
        List<CostoMensual> costosPorMes,
        List<EquipoCorrectivos> topEquiposCorrectivos,
        List<RepuestoUso> topRepuestos,
        List<CargaTecnico> cargaPorTecnico
) {

    public record CostoMensual(LocalDate mes, BigDecimal costoValorizado, BigDecimal costoRepuestos, BigDecimal total) {
    }

    public record EquipoCorrectivos(Long equipoId, String descripcion, String codigo, long cantidad) {
    }

    public record RepuestoUso(String nombre, BigDecimal cantidadTotal, BigDecimal costoTotal, long usos) {
    }

    public record CargaTecnico(Long tecnicoId, String nombre, long cantidadServicios, BigDecimal costoTotal) {
    }
}
