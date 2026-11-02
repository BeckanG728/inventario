package com.taller.common.exception;

/**
 * Se lanza cuando una operación intentaría dejar el stock de un producto en negativo.
 * Extiende {@link IllegalArgumentException} para conservar el contrato de RN206.
 */
public class StockInsuficienteException extends IllegalArgumentException {

    public StockInsuficienteException(String message) {
        super(message);
    }
}
