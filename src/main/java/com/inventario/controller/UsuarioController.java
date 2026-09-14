package com.inventario.controller;

import com.inventario.dto.request.EditarPerfilRequest;
import com.inventario.dto.request.EditarUsuarioRequest;
import com.inventario.dto.request.RegisterRequest;
import com.inventario.dto.request.ResetPasswordRequest;
import com.inventario.dto.response.UsuarioResponse;
import com.inventario.dto.response.UsuarioResumenResponse;
import com.inventario.entity.Rol;
import com.inventario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("@permisoService.tienePermiso(authentication, 'USUARIOS', 'VER')")
    @Operation(summary = "Listar usuarios activos, opcionalmente filtrados por rol (para selectores como técnico responsable)")
    public List<UsuarioResumenResponse> listar(@RequestParam(required = false) Rol rol) {
        return usuarioService.listar(rol);
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios, activos e inactivos, para el panel de administración (solo ADMIN)")
    public List<UsuarioResponse> listarTodos() {
        return usuarioService.listarTodos();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo usuario (solo ADMIN)")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Editar nombre y rol de un usuario (solo ADMIN)")
    public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody EditarUsuarioRequest request) {
        return usuarioService.editar(id, request);
    }

    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar un usuario desactivado (solo ADMIN)")
    public UsuarioResponse activar(@PathVariable Long id) {
        return usuarioService.cambiarEstado(id, true, null);
    }

    @PutMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar un usuario (solo ADMIN, no aplica sobre la propia cuenta ni el último administrador)")
    public UsuarioResponse desactivar(@PathVariable Long id, Authentication authentication) {
        return usuarioService.cambiarEstado(id, false, authentication.getName());
    }

    @PutMapping("/{id}/resetear-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resetear la contraseña de un usuario; el usuario deberá cambiarla en su próximo login (solo ADMIN)")
    public ResponseEntity<Void> resetearPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        usuarioService.resetearPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    @Operation(summary = "Editar el nombre del propio perfil (cualquier usuario autenticado)")
    public UsuarioResponse actualizarPerfilPropio(Authentication authentication, @Valid @RequestBody EditarPerfilRequest request) {
        return usuarioService.actualizarPerfilPropio(authentication.getName(), request);
    }
}
