package com.taller.monitoreo.service.impl;

import com.taller.monitoreo.dto.response.AlertaStock;
import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.compartido.EstadoStock;
import com.taller.monitoreo.service.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final ProductoStockMinimo productoStockMinimo;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaStock> generarAlertas() {
        return productoStockMinimo.consultarBajoStock().stream()
                .map(this::toAlerta)
                .sorted(Comparator.comparing(AlertaStock::cantidadSugerida).reversed()
                        .thenComparing(AlertaStock::nombre))
                .toList();
    }

    @Override
    public String resumir(List<AlertaStock> alertas) {
        if (alertas.isEmpty()) {
            return MENSAJE_SIN_ALERTAS;
        }
        return "Hay " + alertas.size() + " producto(s) con bajo stock";
    }

    private AlertaStock toAlerta(ProductoResponse producto) {
        return new AlertaStock(
                producto.codigo(),
                producto.nombre(),
                producto.stockActual(),
                producto.stockMinimo(),
                EstadoStock.de(producto.stockActual(), producto.stockMinimo())
                        .cantidadSugerida(producto.stockActual(), producto.stockMinimo()),
                producto.proveedorRazonSocial());
    }
}
