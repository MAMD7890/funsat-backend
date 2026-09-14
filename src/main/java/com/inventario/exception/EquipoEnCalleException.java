package com.inventario.exception;

public class EquipoEnCalleException extends RuntimeException {

    public EquipoEnCalleException(String descripcionEquipo) {
        super("El equipo \"" + descripcionEquipo + "\" ya está en una orden de salida sin devolución registrada");
    }
}
