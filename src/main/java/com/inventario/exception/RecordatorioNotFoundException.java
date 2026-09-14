package com.inventario.exception;

public class RecordatorioNotFoundException extends RuntimeException {

    public RecordatorioNotFoundException(Long id) {
        super("No existe un recordatorio con id " + id);
    }
}
