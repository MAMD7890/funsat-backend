package com.inventario.exception;

public class OrdenSalidaItemAccesorioNotFoundException extends RuntimeException {

    public OrdenSalidaItemAccesorioNotFoundException(Long id) {
        super("No existe un accesorio de orden de salida con id " + id);
    }
}
