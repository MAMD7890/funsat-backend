package com.inventario.dto.response;

import com.inventario.entity.Equipo;

public record EquipoResumenResponse(
        Long id,
        String codigo,
        String descripcionEquipo,
        CategoriaCatalogoResponse categoria,
        String marca,
        String modelo,
        String numeroSerie
) {

    public static EquipoResumenResponse from(Equipo equipo) {
        return new EquipoResumenResponse(
                equipo.getId(),
                equipo.getCodigo(),
                equipo.getDescripcionEquipo(),
                CategoriaCatalogoResponse.from(equipo.getCategoria()),
                equipo.getMarca(),
                equipo.getModelo(),
                equipo.getNumeroSerie()
        );
    }
}
