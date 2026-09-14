package com.inventario.exception;

public class CategoriaCatalogoNotFoundException extends RuntimeException {

    public CategoriaCatalogoNotFoundException(Long id) {
        super("No existe una categoría con id " + id);
    }
}
