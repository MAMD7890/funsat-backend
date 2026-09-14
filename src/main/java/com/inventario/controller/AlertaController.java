package com.inventario.controller;

import com.inventario.dto.response.ResultadoAlertaResponse;
import com.inventario.service.AlertaEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas")
public class AlertaController {

    private final AlertaEmailService alertaEmailService;

    @PostMapping("/enviar-resumen")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enviar de inmediato el resumen diario de alertas por correo (solo ADMIN)")
    public ResultadoAlertaResponse enviarResumen() {
        return alertaEmailService.enviarResumenDiario();
    }
}
