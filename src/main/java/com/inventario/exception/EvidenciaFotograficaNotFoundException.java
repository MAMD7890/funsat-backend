package com.inventario.exception;

public class EvidenciaFotograficaNotFoundException extends RuntimeException {

    public EvidenciaFotograficaNotFoundException(Long id) {
        super("No existe una evidencia fotográfica con id " + id);
    }
}
