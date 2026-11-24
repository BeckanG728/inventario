package com.taller.compartido;

/**
 * Clasificación operativa del nivel de stock, compartida por el listado de existencias
 * y el cálculo de alertas. Un producto con stock 0 está SIN_STOCK; con stock mayor que 0
 * pero menor o igual al mínimo está BAJO; en caso contrario está NORMAL.
 */
public enum EstadoStock {

    NORMAL,
    BAJO,
    SIN_STOCK;

    public static EstadoStock de(Integer stockActual, Integer stockMinimo) {
        if (stockActual == null || stockActual == 0) {
            return SIN_STOCK;
        }
        if (stockMinimo != null && stockActual <= stockMinimo) {
            return BAJO;
        }
        return NORMAL;
    }

    /** Cantidad referencial a reponer; 0 para niveles normales o mínimo cero. */
    public int cantidadSugerida(Integer stockActual, Integer stockMinimo) {
        return switch (this) {
            case NORMAL -> 0;
            case SIN_STOCK -> (stockMinimo == null || stockMinimo == 0) ? 0 : stockMinimo;
            case BAJO -> stockMinimo - stockActual;
        };
    }
}
