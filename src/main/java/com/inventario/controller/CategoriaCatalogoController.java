package com.inventario.controller;

import com.inventario.dto.request.CategoriaCatalogoRequest;
import com.inventario.dto.response.CategoriaCatalogoResponse;
import com.inventario.service.CategoriaCatalogoService;
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

/**
 * CRUD del catalogo de categorias de equipo (antes un enum Java fijo). Solo
 * ADMIN puede crear/editar -una categoria mal gestionada afecta a todos los
 * equipos que la usan-, pero listar queda abierto a cualquier usuario
 * autenticado porque el formulario de Equipo la necesita para su selector.
 */
@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías de Equipo")
public class CategoriaCatalogoController {

    private final CategoriaCatalogoService categoriaCatalogoService;

    @GetMapping
    @Operation(summary = "Listar categorías de equipo (por defecto solo activas)")
    public List<CategoriaCatalogoResponse> listar(@RequestParam(defaultValue = "true") boolean soloActivas) {
        return categoriaCatalogoService.listar(soloActivas);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una categoría de equipo (solo ADMIN)")
    public ResponseEntity<CategoriaCatalogoResponse> crear(@Valid @RequestBody CategoriaCatalogoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaCatalogoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una categoría de equipo (solo ADMIN)")
    public CategoriaCatalogoResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaCatalogoRequest request) {
        return categoriaCatalogoService.actualizar(id, request);
    }
}
