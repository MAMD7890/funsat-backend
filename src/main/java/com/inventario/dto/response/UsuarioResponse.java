package com.inventario.dto.response;

import com.inventario.entity.Rol;
import com.inventario.entity.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String username,
        String email,
        Rol rol,
        boolean activo,
        boolean debeCambiarPassword
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.isDebeCambiarPassword()
        );
    }
}
