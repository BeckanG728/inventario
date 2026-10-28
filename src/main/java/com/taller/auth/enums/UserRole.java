package com.taller.auth.enums;

public enum UserRole {
    ADMIN, JEFE_TALLER, MECANICO;

    public boolean validRole() {
        return this == JEFE_TALLER || this == MECANICO;
    }
}
