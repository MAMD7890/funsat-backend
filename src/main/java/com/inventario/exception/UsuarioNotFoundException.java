package com.inventario.exception;

public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(Long id) {
        super("No existe un usuario con id " + id);
    }
}
