package com.inventario.controller;

import com.inventario.dto.response.ReporteResponse;
import com.inventario.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    @Operation(summary = "Reportes agregados: tendencia de costos, equipos con mas correctivos, repuestos mas usados y carga por tecnico")
    public ReporteResponse generar() {
        return reporteService.generar();
    }
}
