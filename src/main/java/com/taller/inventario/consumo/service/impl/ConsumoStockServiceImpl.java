package com.taller.inventario.consumo.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.common.exception.StockInsuficienteException;
import com.taller.inventario.consumo.domain.ConsumoItem;
import com.taller.inventario.consumo.service.ConsumoStockService;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import com.taller.producto.entity.Producto;
import com.taller.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsumoStockServiceImpl implements ConsumoStockService {

    private static final String PRODUCTO_NO_DISPONIBLE = "Producto ID %d no encontrado o inactivo";
    private static final String CANTIDAD_INVALIDA = "La cantidad del producto ID %d debe ser mayor que cero";
    private static final String CANTIDAD_EXCEDIDA = "La cantidad total del producto ID %d excede el máximo permitido";
    private static final String STOCK_INSUFICIENTE =
            "Stock insuficiente para el producto ID %d (%s): solicitado %d, disponible %d";

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    @Transactional
    public List<MovimientoInventario> validarYDescontarStock(List<ConsumoItem> items,
                                                             Usuario usuario,
                                                             Long consumoId,
                                                             String motivo) {
        validarCantidades(items);

        Map<Long, Integer> cantidadesPorProducto = agruparCantidades(items);
        validarDisponibilidad(cantidadesPorProducto);

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (ConsumoItem item : items) {
            Producto producto = productoRepository.findByIdAndActivoTrueForUpdate(item.productoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            PRODUCTO_NO_DISPONIBLE.formatted(item.productoId())));

            int stockAnterior = producto.getStockActual();
            producto.descontarStock(item.cantidad());

            MovimientoInventario movimiento = new MovimientoInventario(
                    TipoMovimiento.CONSUMO,
                    producto,
                    usuario,
                    stockAnterior,
                    producto.getStockActual(),
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
        for (Map.Entry<Long, Integer> entry : cantidadesPorProducto.entrySet()) {
            Long productoId = entry.getKey();
            Producto producto = productoRepository.findById(productoId)
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new IllegalArgumentException(PRODUCTO_NO_DISPONIBLE.formatted(productoId)));

            int solicitado = entry.getValue();
            int disponible = producto.getStockActual();
            if (disponible < solicitado) {
                throw new StockInsuficienteException(
                        STOCK_INSUFICIENTE.formatted(productoId, producto.getNombre(), solicitado, disponible));
            }
        }
    }
}
