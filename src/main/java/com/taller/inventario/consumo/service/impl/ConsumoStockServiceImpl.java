package com.taller.inventario.consumo.service.impl;

import com.taller.catalogo.api.ProductoStock;
import com.taller.catalogo.api.ProductoStock.ResultadoStock;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.unidad.service.ConversorUnidades;
import com.taller.catalogo.unidad.service.ConversorUnidades.ResultadoConversion;
import com.taller.compartido.exception.StockInsuficienteException;
import com.taller.inventario.consumo.domain.ConsumoItem;
import com.taller.inventario.consumo.service.ConsumoStockService;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    private final ProductoStock productoStock;
    private final ConversorUnidades conversorUnidades;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    @Transactional
    public List<MovimientoInventario> validarYDescontarStock(List<ConsumoItem> items,
                                                              Long usuarioId,
                                                              Long consumoId,
                                                              String motivo) {
        validarCantidades(items);

        List<Long> ids = items.stream().map(ConsumoItem::productoId).distinct().sorted().toList();
        // Bloqueo ordenado por id: evita interbloqueos cuando dos consumos llegan en orden inverso.
        Map<Long, ProductoResponse> porId = productoStock.bloquearParaDescuento(ids).stream()
                .collect(Collectors.toMap(ProductoResponse::id, Function.identity()));

        List<ResultadoConversion> conversiones = new ArrayList<>(items.size());
        for (ConsumoItem item : items) {
            ProductoResponse producto = porId.get(item.productoId());
            if (producto == null || !producto.activo()) {
                throw new IllegalArgumentException(PRODUCTO_NO_DISPONIBLE.formatted(item.productoId()));
            }
            conversiones.add(conversorUnidades.convertir(
                    producto,
                    item.cantidad(),
                    item.cantidadExacta(),
                    item.unidadRegistro(),
                    item.factorAplicado(),
                    null));
        }

        Map<Long, Integer> cantidadesPorProducto = agruparCantidades(items, conversiones);
        validarDisponibilidad(cantidadesPorProducto, porId);

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ConsumoItem item = items.get(i);
            ResultadoConversion conversion = conversiones.get(i);
            int cantidadBase = conversion.cantidadBase();
            ResultadoStock resultado = productoStock.descontarStock(item.productoId(), cantidadBase);

            MovimientoInventario movimiento = new MovimientoInventario(
                    TipoMovimiento.CONSUMO,
                    item.productoId(),
                    usuarioId,
                    resultado.stockAnterior(),
                    resultado.stockPosterior(),
                    null,
                    motivo);
            movimiento.setCantidad(cantidadBase);
            movimiento.setCantidadBase(cantidadBase);
            movimiento.setUnidadRegistro(conversion.unidadRegistro());
            movimiento.setFactorAplicado(conversion.factorAplicado());
            movimiento.setConsumoId(consumoId);
            // RN208: snapshot del costo vigente al momento del consumo.
            movimiento.setCostoUnitario(resultado.producto().costoUltimaCompra());

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
            boolean enteroValido = item.cantidad() > 0;
            boolean exactoValido = item.cantidadExacta() != null && item.cantidadExacta().signum() > 0;
            if (!enteroValido && !exactoValido) {
                throw new IllegalArgumentException(CANTIDAD_INVALIDA.formatted(item.productoId()));
            }
        }
    }

    private Map<Long, Integer> agruparCantidades(List<ConsumoItem> items, List<ResultadoConversion> conversiones) {
        Map<Long, Integer> totales = new TreeMap<>();
        for (int i = 0; i < items.size(); i++) {
            Long productoId = items.get(i).productoId();
            try {
                totales.merge(productoId, conversiones.get(i).cantidadBase(), Math::addExact);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(CANTIDAD_EXCEDIDA.formatted(productoId));
            }
        }
        return totales;
    }

    private void validarDisponibilidad(Map<Long, Integer> cantidadesPorProducto,
                                       Map<Long, ProductoResponse> porId) {
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
