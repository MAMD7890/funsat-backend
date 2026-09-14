package com.inventario.excel;

/** Error de una fila puntual del Excel; el resto del archivo se sigue procesando. */
class ImportRowException extends RuntimeException {

    ImportRowException(String message) {
        super(message);
    }
}
