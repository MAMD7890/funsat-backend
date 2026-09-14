package com.inventario.dto.response;

import java.util.List;

public record ImportResultResponse(
        int totalFilas,
        int filasImportadas,
        int filasConError,
        List<ImportFilaError> errores,
        List<String> advertencias
) {
}
