package com.inventario.exception;

public class EquipoNotFoundException extends RuntimeException {

    public EquipoNotFoundException(Long id) {
        super("No existe un equipo con id " + id);
    }
}
