package com.inventario.exception;

public class RepuestoNotFoundException extends RuntimeException {

    public RepuestoNotFoundException(Long id) {
        super("No existe un repuesto con id " + id);
    }
}
