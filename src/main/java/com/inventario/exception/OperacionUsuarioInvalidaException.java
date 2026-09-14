package com.inventario.exception;

public class OperacionUsuarioInvalidaException extends RuntimeException {

    public OperacionUsuarioInvalidaException(String mensaje) {
        super(mensaje);
    }
}
