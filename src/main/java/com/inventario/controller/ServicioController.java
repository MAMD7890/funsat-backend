package com.inventario.controller;

import com.inventario.dto.request.CambiarEstadoServicioRequest;
import com.inventario.dto.request.ServicioRequest;
import com.inventario.dto.response.ServicioResponse;
import com.inventario.entity.EstadoServicio;
import com.inventario.entity.TipoServicio;
import com.inventario.pdf.ServicioPdfService;
import com.inventario.service.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/servicios")
@RequiredArgsConstructor
@Tag(name = "Servicios de Mantenimiento")
public class ServicioController {

    private final ServicioService servicioService;
    private final ServicioPdfService servicioPdfService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'VER')")
    @Operation(summary = "Listar servicios/mantenimientos (paginado, con filtros; ?equipoId= da el historial de un equipo)")
    public Page<ServicioResponse> listar(
            @RequestParam(required = false) Long equipoId,
            @RequestParam(required = false) TipoServicio tipoServicio,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) EstadoServicio estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return servicioService.buscar(equipoId, tipoServicio, tecnicoId, estado, desde, hasta, q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'VER')")
    @Operation(summary = "Obtener un servicio por id")
    public ServicioResponse obtener(@PathVariable Long id) {
        return servicioService.obtener(id);
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'VER')")
    @Operation(summary = "Descargar el PDF del servicio/mantenimiento")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        ServicioResponse servicio = servicioService.obtener(id);
        byte[] contenido = servicioPdfService.generar(servicio);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(servicio.numero() + ".pdf").build().toString())
                .body(contenido);
    }

    @PostMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'CREAR')")
    @Operation(summary = "Registrar un servicio/mantenimiento (con repuestos y checklist opcionales)")
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'EDITAR')")
    @Operation(summary = "Actualizar un servicio/mantenimiento")
    public ServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest request) {
        return servicioService.actualizar(id, request);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'EDITAR')")
    @Operation(summary = "Cambiar solo el estado de un servicio (para el tablero Kanban)")
    public ServicioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoServicioRequest request) {
        return servicioService.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'SERVICIOS', 'ELIMINAR')")
    @Operation(summary = "Eliminar un servicio/mantenimiento (según permisos del rol)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
