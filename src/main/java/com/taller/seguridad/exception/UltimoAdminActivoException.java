package com.taller.seguridad.exception;

public class UltimoAdminActivoException extends RuntimeException {

    public UltimoAdminActivoException() {
        super("No se puede desactivar ni cambiar el rol del último ADMIN activo");
    }
}
