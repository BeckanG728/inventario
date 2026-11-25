package com.taller.inventario.consumo.service.impl;

import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.compartido.ZonaHorariaNegocio;
import com.taller.inventario.api.HistorialMovimientos;
import com.taller.inventario.api.MovimientoDetalle;
import com.taller.inventario.consumo.domain.ConsumoItem;
import com.taller.inventario.consumo.dto.request.ConsumoRequest;
import com.taller.inventario.consumo.dto.response.ConsumoResponse;
import com.taller.inventario.consumo.dto.response.ProductoFrecuenteResponse;
import com.taller.inventario.consumo.entity.Consumo;
import com.taller.inventario.consumo.repository.ConsumoRepository;
import com.taller.inventario.consumo.service.ConsumoService;
import com.taller.inventario.consumo.service.ConsumoStockService;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsumoServiceImpl implements ConsumoService {

    private static final int DIAS_HISTORIAL = 90;
    private static final int LIMITE_PRODUCTOS = 5;

    private final ConsumoRepository consumoRepository;
    private final ConsumoStockService consumoStockService;
    private final HistorialMovimientos historialMovimientos;
    private final ProductoStockMinimo productoStockMinimo;
    private final Clock clock;

    @Override
    @Transactional
    public ConsumoResponse registrar(ConsumoRequest request, Long usuarioId) {
        String finalidad = request.finalidad().trim();
        Consumo consumo = consumoRepository.save(new Consumo(
                Instant.now(clock), finalidad, normalizarReferencia(request.referenciaVehiculoCliente()),
                usuarioId));

        List<ConsumoItem> items = request.items().stream()
                .map(item -> new ConsumoItem(
                        item.productoId(),
                        item.cantidad() == null ? 0 : item.cantidad(),
                        item.unidadRegistro(),
                        item.factorAplicado(),
                        item.cantidadExacta()))
                .toList();

        List<MovimientoInventario> movimientos = consumoStockService.validarYDescontarStock(
                items, usuarioId, consumo.getId(), finalidad);

        return ConsumoResponse.from(consumo, movimientos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoFrecuenteResponse> productosFrecuentes() {
        Instant ahora = Instant.now(clock);
        Instant desde = LocalDate.now(ZonaHorariaNegocio.ZONA_NEGOCIO)
                .minusDays(DIAS_HISTORIAL)
                .atStartOfDay(ZonaHorariaNegocio.ZONA_NEGOCIO)
                .toInstant();

        Map<Long, long[]> frecuencias = new HashMap<>();
        for (MovimientoDetalle movimiento : historialMovimientos.consultarConsumosEntre(desde, ahora)) {
            long[] valores = frecuencias.computeIfAbsent(movimiento.productoId(), id -> new long[2]);
            valores[0]++;
            valores[1] += movimiento.cantidad() == null ? 0 : movimiento.cantidad();
        }

        return productoStockMinimo.consultarActivos().stream()
                .filter(producto -> producto.stockActual() != null && producto.stockActual() > 0)
                .sorted(Comparator
                        .comparingLong((ProductoResponse producto) ->
                                frecuencias.getOrDefault(producto.id(), new long[2])[0])
                        .reversed()
                        .thenComparing(ProductoResponse::nombre, String.CASE_INSENSITIVE_ORDER))
                .limit(LIMITE_PRODUCTOS)
                .map(producto -> {
                    long[] valores = frecuencias.getOrDefault(producto.id(), new long[2]);
                    return ProductoFrecuenteResponse.desde(producto, valores[0], valores[1]);
                })
                .toList();
    }

    private String normalizarReferencia(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return null;
        }
        return referencia.trim();
    }
}
