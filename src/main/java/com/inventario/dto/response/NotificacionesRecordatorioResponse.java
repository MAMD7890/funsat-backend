package com.inventario.dto.response;

import java.util.List;

public record NotificacionesRecordatorioResponse(
        List<RecordatorioResponse> vencidos,
        List<RecordatorioResponse> proximos
) {
}
