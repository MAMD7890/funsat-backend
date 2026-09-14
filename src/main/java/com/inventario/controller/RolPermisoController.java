package com.inventario.controller;

import com.inventario.dto.request.ActualizarPermisosRequest;
import com.inventario.dto.response.RolPermisosResponse;
import com.inventario.entity.Rol;
import com.inventario.service.RolPermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestion de la matriz de permisos por rol. Deliberadamente hardcoded a
 * ADMIN (no pasa por PermisoService/la matriz dinamica) para que un ADMIN
 * nunca pueda editar la matriz y quedar el mismo bloqueado fuera de esta
 * pantalla.
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Roles y Permisos")
public class RolPermisoController {

    private final RolPermisoService rolPermisoService;

    @GetMapping
    @Operation(summary = "Ver la matriz de permisos de los 3 roles (solo ADMIN)")
    public List<RolPermisosResponse> listar() {
        return rolPermisoService.listarMatriz();
    }

    @PutMapping("/{rol}/permisos")
    @Operation(summary = "Actualizar los permisos de un rol (solo ADMIN)")
    public RolPermisosResponse actualizar(@PathVariable Rol rol, @Valid @RequestBody ActualizarPermisosRequest request) {
        return rolPermisoService.actualizar(rol, request);
    }
}
