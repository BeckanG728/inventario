package com.taller.reporte.service;

import com.taller.alerta.dto.response.AlertaStock;
import com.taller.reporte.dto.response.ProductoBajoStock;
import com.taller.reporte.dto.response.ProveedorBajoStock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Lógica pura: agrupa las alertas de bajo stock por proveedor (orden alfabético). */
public final class BajoStockAgrupador {

    private BajoStockAgrupador() {
    }

    public static List<ProveedorBajoStock> agrupar(List<AlertaStock> alertas) {
        Map<String, List<ProductoBajoStock>> porProveedor = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (AlertaStock a : alertas) {
            porProveedor
                    .computeIfAbsent(a.proveedor(), k -> new ArrayList<>())
                    .add(new ProductoBajoStock(
                            a.codigo(), a.nombre(), a.stockActual(), a.stockMinimo(), a.cantidadSugerida()));
        }
        return porProveedor.entrySet().stream()
                .map(e -> new ProveedorBajoStock(e.getKey(), List.copyOf(e.getValue())))
                .toList();
    }
}