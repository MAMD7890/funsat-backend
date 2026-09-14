package com.inventario.controller;

import com.inventario.dto.request.CrearRecordatorioRequest;
import com.inventario.dto.request.EditarRecordatorioRequest;
import com.inventario.dto.response.NotificacionesRecordatorioResponse;
import com.inventario.dto.response.RecordatorioResponse;
import com.inventario.service.RecordatorioMantenimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/recordatorios")
@RequiredArgsConstructor
@Tag(name = "Recordatorios de Mantenimiento")
public class RecordatorioMantenimientoController {

    private final RecordatorioMantenimientoService recordatorioService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'RECORDATORIOS', 'VER')")
    @Operation(summary = "Listar recordatorios programados en un rango de fechas (para el calendario)")
    public List<RecordatorioResponse> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return recordatorioService.listarPorRango(desde, hasta);
    }

    @GetMapping("/notificaciones")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'RECORDATORIOS', 'VER')")
    @Operation(summary = "Recordatorios vencidos y próximos a vencer (para la campana de notificaciones)")
    public NotificacionesRecordatorioResponse notificaciones() {
        return recordatorioService.notificaciones();
    }

    @PostMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'RECORDATORIOS', 'CREAR')")
    @Operation(summary = "Programar un nuevo recordatorio de mantenimiento para un equipo")
    public ResponseEntity<RecordatorioResponse> crear(@Valid @RequestBody CrearRecordatorioRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recordatorioService.crear(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'RECORDATORIOS', 'EDITAR')")
    @Operation(summary = "Editar un recordatorio de mantenimiento")
    public RecordatorioResponse editar(@PathVariable Long id, @Valid @RequestBody EditarRecordatorioRequest request) {
        return recordatorioService.editar(id, request);
    }

    @PutMapping("/{id}/completar")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'RECORDATORIOS', 'EDITAR')")
    @Operation(summary = "Marcar un recordatorio como cumplido (agenda el siguiente si tiene recurrencia)")
    public RecordatorioResponse completar(@PathVariable Long id) {
        return recordatorioService.completar(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'RECORDATORIOS', 'ELIMINAR')")
    @Operation(summary = "Eliminar un recordatorio (según permisos del rol)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        recordatorioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
