package com.taller.alerta.service.impl;

import com.taller.alerta.dto.response.AlertaStock;
import com.taller.alerta.service.AlertaService;
import com.taller.producto.entity.Producto;
import com.taller.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaStock> generarAlertas() {
        return productoRepository.findAllByActivoTrue().stream()
                .filter(this::tieneBajoStock)
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

    /** RN302: bajo stock cuando stock_actual <= stock_minimo. */
    private boolean tieneBajoStock(Producto producto) {
        return producto.getStockActual() <= producto.getStockMinimo();
    }

    private AlertaStock toAlerta(Producto producto) {
        return new AlertaStock(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getStockMinimo() - producto.getStockActual(),
                producto.getProveedor().getRazonSocial());
    }
}