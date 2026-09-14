package com.inventario.dto.response;

import com.inventario.entity.Usuario;

public record UsuarioResumenResponse(
        Long id,
        String nombre,
        String username
) {

    public static UsuarioResumenResponse from(Usuario usuario) {
        return new UsuarioResumenResponse(usuario.getId(), usuario.getNombre(), usuario.getUsername());
    }
}
