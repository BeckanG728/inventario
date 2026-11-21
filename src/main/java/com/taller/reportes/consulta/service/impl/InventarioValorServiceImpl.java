package com.taller.reportes.consulta.service.impl;

import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.reportes.dto.response.ValorInventarioResponse;
import com.taller.reportes.consulta.service.InventarioValorService;
import com.taller.reportes.consulta.service.ValorInventarioCalculator;
import com.taller.reportes.consulta.service.ValorInventarioCalculator.Item;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioValorServiceImpl implements InventarioValorService {

    private final ProductoStockMinimo productoStockMinimo;

    @Override
    @Transactional(readOnly = true)
    public ValorInventarioResponse consultar() {
        List<Item> items = productoStockMinimo.consultarActivos().stream()
                .map(p -> new Item(p.codigo(), p.nombre(), p.stockActual(), p.costoUltimaCompra()))
                .toList();
        return ValorInventarioCalculator.calcular(items);
    }
}
