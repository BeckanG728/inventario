package com.taller.reporte.service.impl;

import com.taller.reporte.dto.response.ValorInventarioResponse;
import com.taller.reporte.repository.ProductoReporteRepository;
import com.taller.reporte.service.InventarioValorService;
import com.taller.reporte.service.ValorInventarioCalculator;
import com.taller.reporte.service.ValorInventarioCalculator.Item;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioValorServiceImpl implements InventarioValorService {

    private final ProductoReporteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ValorInventarioResponse consultar() {
        List<Item> items = repository.findByActivoTrueOrderByNombreAsc().stream()
                .map(p -> new Item(p.getCodigo(), p.getNombre(), p.getStockActual(), p.getCostoUltimaCompra()))
                .toList();
        return ValorInventarioCalculator.calcular(items);
    }
}