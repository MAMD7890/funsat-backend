package com.inventario.dto.response;

import com.inventario.entity.CategoriaCatalogo;

public record CategoriaCatalogoResponse(
        Long id,
        String nombre,
        boolean requiereNumeroSerie,
        int orden,
        boolean activo
) {

    public static CategoriaCatalogoResponse from(CategoriaCatalogo categoria) {
        return new CategoriaCatalogoResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.isRequiereNumeroSerie(),
                categoria.getOrden(),
                categoria.isActivo()
        );
    }
}
