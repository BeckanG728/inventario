package com.taller.inventario.error;

public class ConflictoIdempotenciaException extends RuntimeException {

    public ConflictoIdempotenciaException() {
        super("La clave de idempotencia corresponde a otra solicitud");
    }
}
