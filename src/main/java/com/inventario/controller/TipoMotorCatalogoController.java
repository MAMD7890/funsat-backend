package com.inventario.controller;

import com.inventario.dto.request.TipoMotorCatalogoRequest;
import com.inventario.dto.response.TipoMotorCatalogoResponse;
import com.inventario.service.TipoMotorCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CRUD del catalogo de tipos de motor de equipo (antes un enum Java fijo). Mismo criterio de permisos que CategoriaCatalogoController. */
@RestController
@RequestMapping("/tipos-motor")
@RequiredArgsConstructor
@Tag(name = "Tipos de Motor")
public class TipoMotorCatalogoController {

    private final TipoMotorCatalogoService tipoMotorCatalogoService;

    @GetMapping
    @Operation(summary = "Listar tipos de motor (por defecto solo activos)")
    public List<TipoMotorCatalogoResponse> listar(@RequestParam(defaultValue = "true") boolean soloActivos) {
        return tipoMotorCatalogoService.listar(soloActivos);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un tipo de motor (solo ADMIN)")
    public ResponseEntity<TipoMotorCatalogoResponse> crear(@Valid @RequestBody TipoMotorCatalogoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoMotorCatalogoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un tipo de motor (solo ADMIN)")
    public TipoMotorCatalogoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TipoMotorCatalogoRequest request) {
        return tipoMotorCatalogoService.actualizar(id, request);
    }
}
