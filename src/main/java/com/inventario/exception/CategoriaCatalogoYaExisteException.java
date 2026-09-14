package com.inventario.exception;

public class CategoriaCatalogoYaExisteException extends RuntimeException {

    public CategoriaCatalogoYaExisteException(String nombre) {
        super("Ya existe una categoría con el nombre '" + nombre + "'");
    }
}
