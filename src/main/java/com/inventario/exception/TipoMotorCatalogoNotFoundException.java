package com.inventario.exception;

public class TipoMotorCatalogoNotFoundException extends RuntimeException {

    public TipoMotorCatalogoNotFoundException(Long id) {
        super("No existe un tipo de motor con id " + id);
    }
}
