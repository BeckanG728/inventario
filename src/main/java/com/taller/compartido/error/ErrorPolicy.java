package com.taller.compartido.error;

/** Política que traduce un tipo de excepción al contrato de errores. */
public interface ErrorPolicy {

    boolean supports(Throwable error);

    ErrorDefinition resolve(Throwable error);
}
