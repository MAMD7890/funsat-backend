package com.inventario.dto.response;

import com.inventario.entity.ChecklistItemCatalogo;

public record ChecklistItemResponse(
        Long id,
        CategoriaCatalogoResponse categoria,
        String nombre,
        int orden,
        boolean activo
) {

    public static ChecklistItemResponse from(ChecklistItemCatalogo item) {
        return new ChecklistItemResponse(
                item.getId(),
                CategoriaCatalogoResponse.from(item.getCategoria()),
                item.getNombre(),
                item.getOrden(),
                item.isActivo()
        );
    }
}
