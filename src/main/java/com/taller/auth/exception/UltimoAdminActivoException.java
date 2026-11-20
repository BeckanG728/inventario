package com.taller.auth.exception;

public class UltimoAdminActivoException extends RuntimeException {

    public UltimoAdminActivoException() {
        super("No se puede desactivar ni cambiar el rol del último ADMIN activo");
    }
}
