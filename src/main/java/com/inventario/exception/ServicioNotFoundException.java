package com.inventario.exception;

public class ServicioNotFoundException extends RuntimeException {

    public ServicioNotFoundException(Long id) {
        super("No existe un servicio con id " + id);
    }
}
