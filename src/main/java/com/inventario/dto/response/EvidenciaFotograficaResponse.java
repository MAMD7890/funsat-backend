package com.inventario.dto.response;

import com.inventario.entity.EvidenciaFotografica;
import com.inventario.entity.TipoEvidencia;

import java.time.LocalDateTime;

public record EvidenciaFotograficaResponse(
        Long id,
        Long servicioId,
        TipoEvidencia tipoEvidencia,
        String nombreOriginal,
        String contentType,
        Long tamanoBytes,
        String descripcion,
        LocalDateTime fecha,
        String url
) {

    public static EvidenciaFotograficaResponse from(EvidenciaFotografica evidencia) {
        Long servicioId = evidencia.getServicio().getId();
        return new EvidenciaFotograficaResponse(
                evidencia.getId(),
                servicioId,
                evidencia.getTipoEvidencia(),
                evidencia.getNombreOriginal(),
                evidencia.getContentType(),
                evidencia.getTamanoBytes(),
                evidencia.getDescripcion(),
                evidencia.getFecha(),
                "/servicios/" + servicioId + "/evidencias/" + evidencia.getId() + "/archivo"
        );
    }
}
