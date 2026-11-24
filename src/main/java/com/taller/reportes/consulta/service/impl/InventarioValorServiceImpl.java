package com.taller.reportes.consulta.service.impl;

import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.reportes.dto.response.ValorInventarioDetalle;
import com.taller.reportes.dto.response.ValorInventarioPaginaResponse;
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

    private static final int TAMANO_PAGINA_MAXIMO = 100;

    @Override
    @Transactional(readOnly = true)
    public ValorInventarioResponse consultar() {
        List<Item> items = productoStockMinimo.consultarActivos().stream()
                .map(p -> new Item(p.codigo(), p.nombre(), p.stockActual(), p.costoUltimaCompra()))
                .toList();
        return ValorInventarioCalculator.calcular(items);
    }

    @Override
    @Transactional(readOnly = true)
    public ValorInventarioPaginaResponse consultarPagina(int page, int size) {
        ValorInventarioResponse completo = consultar();
        int numeroPagina = Math.max(page, 0);
        int tamanio = Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO);
        List<ValorInventarioDetalle> detalle = completo.detalle();

        int desde = Math.min(numeroPagina * tamanio, detalle.size());
        int hasta = Math.min(desde + tamanio, detalle.size());
        int totalPaginas = detalle.isEmpty() ? 0 : (int) Math.ceil((double) detalle.size() / tamanio);

        return new ValorInventarioPaginaResponse(
                completo.totalGeneral(),
                completo.totalProductosConCosto(),
                completo.productosSinCosto(),
                List.copyOf(detalle.subList(desde, hasta)),
                numeroPagina,
                tamanio,
                detalle.size(),
                totalPaginas);
    }
}
