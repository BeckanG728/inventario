package com.taller.consumo.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.consumo.dto.request.ConsumoItemRequest;
import com.taller.consumo.dto.request.ConsumoRequest;
import com.taller.consumo.dto.response.ConsumoResponse;
import com.taller.consumo.entity.Consumo;
import com.taller.consumo.repository.ConsumoRepository;
import com.taller.consumo.service.ConsumoService;
import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.movimiento.repository.MovimientoInventarioRepository;
import com.taller.producto.entity.Producto;
import com.taller.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumoServiceImpl implements ConsumoService {

    private static final String PRODUCTO_NO_DISPONIBLE = "Producto ID %d no encontrado o inactivo";

    private final ConsumoRepository consumoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;
    private final Clock clock;

    @Override
    @Transactional
    public ConsumoResponse registrar(ConsumoRequest request, Usuario usuario) {
        String finalidad = request.finalidad().trim();
        Consumo consumo = consumoRepository.save(new Consumo(
                Instant.now(clock), finalidad, normalizarReferencia(request.referenciaVehiculoCliente()), usuario));

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (ConsumoItemRequest item : request.items()) {
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
                    finalidad);
            movimiento.setCantidad(item.cantidad());
            movimiento.setConsumoId(consumo.getId());

            movimientos.add(movimientoInventarioRepository.save(movimiento));
        }

        return ConsumoResponse.from(consumo, movimientos);
    }

    private String normalizarReferencia(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return null;
        }
        return referencia.trim();
    }
}
