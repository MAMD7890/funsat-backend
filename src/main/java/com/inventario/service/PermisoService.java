package com.inventario.service;

import com.inventario.entity.Accion;
import com.inventario.entity.Modulo;
import com.inventario.entity.Rol;
import com.inventario.repository.RolPermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultado desde @PreAuthorize vía SpEL (ej. "@permisoService.tienePermiso(authentication,
 * 'EQUIPOS', 'ELIMINAR')") para reemplazar los hasRole(...) estaticos por una
 * matriz rol -> modulo -> accion editable desde /api/roles.
 */
@Service("permisoService")
@RequiredArgsConstructor
public class PermisoService {

    private final RolPermisoRepository rolPermisoRepository;

    @Transactional(readOnly = true)
    public boolean tienePermiso(Authentication authentication, String modulo, String accion) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Rol rol = extraerRol(authentication);
        if (rol == null) {
            return false;
        }

        return rolPermisoRepository.existsByRolAndModuloAndAccionAndPermitidoTrue(
                rol, Modulo.valueOf(modulo), Accion.valueOf(accion));
    }

    private Rol extraerRol(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .map(Rol::valueOf)
                .orElse(null);
    }
}
