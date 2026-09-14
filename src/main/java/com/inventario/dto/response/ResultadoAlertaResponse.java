package com.inventario.dto.response;

public record ResultadoAlertaResponse(
        boolean enviado,
        String mensaje,
        int destinatarios
) {
}
