package com.inventario.controller;

import com.inventario.dto.request.EquipoRequest;
import com.inventario.dto.response.EquipoResponse;
import com.inventario.entity.EstadoEquipo;
import com.inventario.entity.Propiedad;
import com.inventario.service.EquipoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/equipos")
@RequiredArgsConstructor
@Tag(name = "Equipos")
public class EquipoController {

    private final EquipoService equipoService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'VER')")
    @Operation(summary = "Listar equipos (paginado, con filtros opcionales)")
    public Page<EquipoResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoEquipo estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Propiedad propiedad,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return equipoService.buscar(categoriaId, estado, clienteId, propiedad, q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'VER')")
    @Operation(summary = "Obtener un equipo por id")
    public EquipoResponse obtener(@PathVariable Long id) {
        return equipoService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'CREAR')")
    @Operation(summary = "Registrar un equipo (propio o externo)")
    public ResponseEntity<EquipoResponse> crear(@Valid @RequestBody EquipoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'EDITAR')")
    @Operation(summary = "Actualizar un equipo")
    public EquipoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EquipoRequest request) {
        return equipoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'EQUIPOS', 'ELIMINAR')")
    @Operation(summary = "Eliminar un equipo (según permisos del rol)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
