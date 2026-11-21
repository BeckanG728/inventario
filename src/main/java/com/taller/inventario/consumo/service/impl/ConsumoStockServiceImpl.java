package com.taller.inventario.consumo.service.impl;

import com.taller.catalogo.api.ProductoStock;
import com.taller.catalogo.api.ProductoStock.ResultadoStock;
import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.compartido.exception.StockInsuficienteException;
import com.taller.inventario.consumo.domain.ConsumoItem;
import com.taller.inventario.consumo.service.ConsumoStockService;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsumoStockServiceImpl implements ConsumoStockService {

    private static final String PRODUCTO_NO_DISPONIBLE = "Producto ID %d no encontrado o inactivo";
    private static final String CANTIDAD_INVALIDA = "La cantidad del producto ID %d debe ser mayor que cero";
    private static final String CANTIDAD_EXCEDIDA = "La cantidad total del producto ID %d excede el máximo permitido";
    private static final String STOCK_INSUFICIENTE =
            "Stock insuficiente para el producto ID %d (%s): solicitado %d, disponible %d";

    private final ProductoStockMinimo productoStockMinimo;
    private final ProductoStock productoStock;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    @Transactional
    public List<MovimientoInventario> validarYDescontarStock(List<ConsumoItem> items,
                                                             Long usuarioId,
                                                             Long consumoId,
                                                             String motivo) {
        validarCantidades(items);

        Map<Long, Integer> cantidadesPorProducto = agruparCantidades(items);
        validarDisponibilidad(cantidadesPorProducto);

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (ConsumoItem item : items) {
            ResultadoStock resultado = productoStock.descontarStock(item.productoId(), item.cantidad());

            MovimientoInventario movimiento = new MovimientoInventario(
                    TipoMovimiento.CONSUMO,
                    item.productoId(),
                    usuarioId,
                    resultado.stockAnterior(),
                    resultado.stockPosterior(),
                    null,
                    motivo);
            movimiento.setCantidad(item.cantidad());
            movimiento.setConsumoId(consumoId);

            movimientos.add(movimientoInventarioRepository.save(movimiento));
        }
        return movimientos;
    }

    private void validarCantidades(List<ConsumoItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un producto");
        }
        for (ConsumoItem item : items) {
            if (item == null || item.productoId() == null) {
                throw new IllegalArgumentException("El producto es obligatorio en todos los ítems");
            }
            if (item.cantidad() <= 0) {
                throw new IllegalArgumentException(CANTIDAD_INVALIDA.formatted(item.productoId()));
            }
        }
    }

    /** Agrupa por producto para validar el total cuando un producto se repite en varios ítems. */
    private Map<Long, Integer> agruparCantidades(List<ConsumoItem> items) {
        Map<Long, Integer> totales = new LinkedHashMap<>();
        for (ConsumoItem item : items) {
            try {
                totales.merge(item.productoId(), item.cantidad(), Math::addExact);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(CANTIDAD_EXCEDIDA.formatted(item.productoId()));
            }
        }
        return totales;
    }

    private void validarDisponibilidad(Map<Long, Integer> cantidadesPorProducto) {
        Map<Long, ProductoResponse> porId = productoStockMinimo
                .consultarPorIds(new ArrayList<>(cantidadesPorProducto.keySet())).stream()
                .collect(Collectors.toMap(ProductoResponse::id, Function.identity()));

        for (Map.Entry<Long, Integer> entry : cantidadesPorProducto.entrySet()) {
            Long productoId = entry.getKey();
            ProductoResponse producto = porId.get(productoId);
            if (producto == null || !producto.activo()) {
                throw new IllegalArgumentException(PRODUCTO_NO_DISPONIBLE.formatted(productoId));
            }

            int solicitado = entry.getValue();
            int disponible = producto.stockActual();
            if (disponible < solicitado) {
                throw new StockInsuficienteException(
                        STOCK_INSUFICIENTE.formatted(productoId, producto.nombre(), solicitado, disponible));
            }
        }
    }
}
