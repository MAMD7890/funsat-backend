package com.inventario.dto.response;

import com.inventario.entity.Cliente;
import com.inventario.entity.TipoCliente;

public record ClienteResponse(
        Long id,
        String nombre,
        TipoCliente tipo,
        String telefono,
        String email
) {

    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getTipo(),
                cliente.getTelefono(),
                cliente.getEmail()
        );
    }
}
