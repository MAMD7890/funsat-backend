package com.inventario.exception;

public class DocumentoEquipoNotFoundException extends RuntimeException {

    public DocumentoEquipoNotFoundException(Long id) {
        super("No existe un documento de equipo con id " + id);
    }
}
