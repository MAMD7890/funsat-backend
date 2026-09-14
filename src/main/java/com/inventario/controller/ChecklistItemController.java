package com.inventario.controller;

import com.inventario.dto.request.ChecklistItemRequest;
import com.inventario.dto.response.ChecklistItemResponse;
import com.inventario.service.ChecklistItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/checklist-items")
@RequiredArgsConstructor
@Tag(name = "Checklist de Mantenimiento")
public class ChecklistItemController {

    private final ChecklistItemService checklistItemService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CHECKLIST', 'VER')")
    @Operation(summary = "Listar ítems de checklist, opcionalmente filtrados por categoría")
    public List<ChecklistItemResponse> listar(@RequestParam(required = false) Long categoriaId) {
        return checklistItemService.listar(categoriaId);
    }

    @PostMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CHECKLIST', 'CREAR')")
    @Operation(summary = "Crear un ítem de checklist para una categoría")
    public ResponseEntity<ChecklistItemResponse> crear(@Valid @RequestBody ChecklistItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checklistItemService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CHECKLIST', 'EDITAR')")
    @Operation(summary = "Actualizar un ítem de checklist")
    public ChecklistItemResponse actualizar(@PathVariable Long id, @Valid @RequestBody ChecklistItemRequest request) {
        return checklistItemService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CHECKLIST', 'ELIMINAR')")
    @Operation(summary = "Eliminar un ítem de checklist (según permisos del rol)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        checklistItemService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
