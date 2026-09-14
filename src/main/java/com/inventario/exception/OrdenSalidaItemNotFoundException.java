package com.inventario.exception;

public class OrdenSalidaItemNotFoundException extends RuntimeException {

    public OrdenSalidaItemNotFoundException(Long id) {
        super("No existe un ítem de orden de salida con id " + id);
    }
}
