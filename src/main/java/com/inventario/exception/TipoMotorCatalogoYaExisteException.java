package com.inventario.exception;

public class TipoMotorCatalogoYaExisteException extends RuntimeException {

    public TipoMotorCatalogoYaExisteException(String nombre) {
        super("Ya existe un tipo de motor con el nombre '" + nombre + "'");
    }
}
