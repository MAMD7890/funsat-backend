package com.inventario.exception;

public class OrdenSalidaNotFoundException extends RuntimeException {

    public OrdenSalidaNotFoundException(Long id) {
        super("No existe una orden de salida con id " + id);
    }
}
