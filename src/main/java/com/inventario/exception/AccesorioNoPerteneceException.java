package com.inventario.exception;

public class AccesorioNoPerteneceException extends RuntimeException {

    public AccesorioNoPerteneceException(Long accesorioId, String descripcionEquipo) {
        super("El accesorio con id " + accesorioId + " no pertenece al equipo \"" + descripcionEquipo + "\"");
    }
}
