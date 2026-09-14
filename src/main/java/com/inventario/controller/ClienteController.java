package com.inventario.controller;

import com.inventario.dto.request.ClienteRequest;
import com.inventario.dto.response.ClienteResponse;
import com.inventario.service.ClienteService;
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
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CLIENTES', 'VER')")
    @Operation(summary = "Listar clientes (paginado)")
    public Page<ClienteResponse> listar(@RequestParam(required = false) String q,
                                         @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return clienteService.listar(q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CLIENTES', 'VER')")
    @Operation(summary = "Obtener un cliente por id")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CLIENTES', 'CREAR')")
    @Operation(summary = "Crear un cliente")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CLIENTES', 'EDITAR')")
    @Operation(summary = "Actualizar un cliente")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'CLIENTES', 'ELIMINAR')")
    @Operation(summary = "Eliminar un cliente (según permisos del rol)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
