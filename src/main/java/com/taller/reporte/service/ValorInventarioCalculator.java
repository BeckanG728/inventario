package com.taller.reporte.service;

import com.taller.reporte.dto.response.ValorInventarioDetalle;
import com.taller.reporte.dto.response.ValorInventarioResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Lógica pura (sin Spring ni BD): RN407 y RN408. */
public final class ValorInventarioCalculator {

    public record Item(String codigo, String nombre, int stockActual, BigDecimal costoUltimaCompra) {
    }

    private ValorInventarioCalculator() {
    }

    public static ValorInventarioResponse calcular(List<Item> items) {
        BigDecimal total = BigDecimal.ZERO;
        int conCosto = 0;
        int sinCosto = 0;
        List<ValorInventarioDetalle> detalle = new ArrayList<>();

        for (Item item : items) {
            if (item.costoUltimaCompra() == null) {
                // RN408: se identifica, se excluye del total y se informa la cantidad
                sinCosto++;
                detalle.add(new ValorInventarioDetalle(item.codigo(), item.nombre(), item.stockActual(),
                        null, scale(BigDecimal.ZERO)));
                continue;
            }
            conCosto++;
            BigDecimal valor = item.stockActual() > 0
                    ? scale(item.costoUltimaCompra().multiply(BigDecimal.valueOf(item.stockActual())))
                    : scale(BigDecimal.ZERO);
            total = total.add(valor);   // RN407: stock_actual * costo_ultima_compra
            detalle.add(new ValorInventarioDetalle(item.codigo(), item.nombre(), item.stockActual(),
                    item.costoUltimaCompra(), valor));
        }
        return new ValorInventarioResponse(scale(total), conCosto, sinCosto, List.copyOf(detalle));
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}