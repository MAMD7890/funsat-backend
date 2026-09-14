package com.inventario.exception;

public class ChecklistItemCategoriaMismatchException extends RuntimeException {

    public ChecklistItemCategoriaMismatchException(Long itemId, String categoriaEquipo) {
        super("El ítem de checklist " + itemId + " no aplica a la categoría " + categoriaEquipo + " del equipo");
    }
}
