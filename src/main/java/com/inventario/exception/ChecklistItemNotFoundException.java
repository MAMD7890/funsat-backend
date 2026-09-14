package com.inventario.exception;

public class ChecklistItemNotFoundException extends RuntimeException {

    public ChecklistItemNotFoundException(Long id) {
        super("No existe un ítem de checklist con id " + id);
    }
}
