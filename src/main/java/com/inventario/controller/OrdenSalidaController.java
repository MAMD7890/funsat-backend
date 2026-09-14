package com.inventario.controller;

import com.inventario.dto.request.OrdenSalidaRequest;
import com.inventario.dto.request.RegistrarDevolucionRequest;
import com.inventario.dto.response.OrdenSalidaResponse;
import com.inventario.pdf.OrdenSalidaPdfService;
import com.inventario.service.OrdenSalidaService;
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
@RequestMapping("/ordenes-salida")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Equipos (Órdenes de Salida)")
public class OrdenSalidaController {

    private final OrdenSalidaService ordenSalidaService;
    private final OrdenSalidaPdfService ordenSalidaPdfService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'VER')")
    @Operation(summary = "Listar órdenes de salida (paginado, con filtros opcionales)")
    public Page<OrdenSalidaResponse> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "fechaSalida", direction = Sort.Direction.DESC) Pageable pageable) {
        return ordenSalidaService.buscar(clienteId, desde, hasta, q, pageable);
    }

    @GetMapping("/equipos-en-calle")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'VER')")
    @Operation(summary = "IDs de equipos actualmente fuera (sin devolución registrada), para excluirlos del selector")
    public List<Long> equiposEnCalle() {
        return ordenSalidaService.equiposEnCalle();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'VER')")
    @Operation(summary = "Obtener una orden de salida por id")
    public OrdenSalidaResponse obtener(@PathVariable Long id) {
        return ordenSalidaService.obtener(id);
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'VER')")
    @Operation(summary = "Descargar el PDF de la orden de salida")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        OrdenSalidaResponse orden = ordenSalidaService.obtener(id);
        byte[] contenido = ordenSalidaPdfService.generar(orden);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(orden.numero() + ".pdf").build().toString())
                .body(contenido);
    }

    @PostMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'CREAR')")
    @Operation(summary = "Registrar una orden de salida de equipos (uno o varios equipos hacia un mismo cliente)")
    public ResponseEntity<OrdenSalidaResponse> crear(@Valid @RequestBody OrdenSalidaRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenSalidaService.crear(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'EDITAR')")
    @Operation(summary = "Editar una orden de salida")
    public OrdenSalidaResponse actualizar(@PathVariable Long id, @Valid @RequestBody OrdenSalidaRequest request) {
        return ordenSalidaService.actualizar(id, request);
    }

    @PutMapping("/{id}/items/{itemId}/devolucion")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'EDITAR')")
    @Operation(summary = "Registrar la devolución (entrada) de un equipo específico de la orden")
    public OrdenSalidaResponse registrarDevolucion(@PathVariable Long id, @PathVariable Long itemId,
                                                     @Valid @RequestBody RegistrarDevolucionRequest request) {
        return ordenSalidaService.registrarDevolucion(id, itemId, request);
    }

    @PutMapping("/{id}/items/{itemId}/accesorios/{accesorioAsociacionId}/devolucion")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'EDITAR')")
    @Operation(summary = "Registrar la devolución (entrada) de un accesorio que quedó pendiente al devolver su equipo")
    public OrdenSalidaResponse registrarDevolucionAccesorio(@PathVariable Long id, @PathVariable Long itemId,
                                                              @PathVariable Long accesorioAsociacionId,
                                                              @Valid @RequestBody RegistrarDevolucionRequest request) {
        return ordenSalidaService.registrarDevolucionAccesorio(id, itemId, accesorioAsociacionId, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'MOVIMIENTOS', 'ELIMINAR')")
    @Operation(summary = "Eliminar una orden de salida (según permisos del rol)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ordenSalidaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
