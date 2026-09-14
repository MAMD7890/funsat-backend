package com.inventario.controller;

import com.inventario.dto.request.RepuestoRequest;
import com.inventario.dto.response.RepuestoResponse;
import com.inventario.service.RepuestoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repuestos")
@RequiredArgsConstructor
@Tag(name = "Repuestos")
public class RepuestoController {

    private final RepuestoService repuestoService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'REPUESTOS', 'VER')")
    @Operation(summary = "Listar repuestos (paginado)")
    public Page<RepuestoResponse> listar(@PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return repuestoService.listar(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'REPUESTOS', 'VER')")
    @Operation(summary = "Obtener un repuesto por id")
    public RepuestoResponse obtener(@PathVariable Long id) {
        return repuestoService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'REPUESTOS', 'CREAR')")
    @Operation(summary = "Crear un repuesto")
    public ResponseEntity<RepuestoResponse> crear(@Valid @RequestBody RepuestoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repuestoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'REPUESTOS', 'EDITAR')")
    @Operation(summary = "Actualizar un repuesto")
    public RepuestoResponse actualizar(@PathVariable Long id, @Valid @RequestBody RepuestoRequest request) {
        return repuestoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'REPUESTOS', 'ELIMINAR')")
    @Operation(summary = "Eliminar un repuesto (según permisos del rol)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        repuestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
