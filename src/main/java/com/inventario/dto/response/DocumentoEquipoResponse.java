package com.inventario.dto.response;

import com.inventario.entity.DocumentoEquipo;
import com.inventario.entity.TipoDocumentoEquipo;

import java.time.LocalDateTime;

public record DocumentoEquipoResponse(
        Long id,
        Long equipoId,
        TipoDocumentoEquipo tipoDocumento,
        String nombreOriginal,
        String contentType,
        Long tamanoBytes,
        String descripcion,
        LocalDateTime fecha,
        String url
) {

    public static DocumentoEquipoResponse from(DocumentoEquipo documento) {
        Long equipoId = documento.getEquipo().getId();
        return new DocumentoEquipoResponse(
                documento.getId(),
                equipoId,
                documento.getTipoDocumento(),
                documento.getNombreOriginal(),
                documento.getContentType(),
                documento.getTamanoBytes(),
                documento.getDescripcion(),
                documento.getFecha(),
                "/equipos/" + equipoId + "/documentos/" + documento.getId() + "/archivo"
        );
    }
}
