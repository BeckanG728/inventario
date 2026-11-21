package com.taller.inventario.api.impl;

import com.taller.inventario.api.PreparacionInventario;
import com.taller.inventario.api.PreparacionInventario.ConsumoDemo;
import com.taller.inventario.api.PreparacionInventario.MovimientoDemo;
import com.taller.inventario.consumo.entity.Consumo;
import com.taller.inventario.consumo.repository.ConsumoRepository;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PreparacionInventarioImpl implements PreparacionInventario {

    private static final int TAMANO_LOTE = 500;

    private final ConsumoRepository consumoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    @Override
    @Transactional
    public Long crearConsumo(ConsumoDemo consumo) {
        return consumoRepository.save(new Consumo(
                consumo.fecha(), consumo.finalidad(), consumo.referencia(), consumo.usuarioId())).getId();
    }

    @Override
    @Transactional
    public void guardarMovimientos(List<MovimientoDemo> movimientos) {
        for (int i = 0; i < movimientos.size(); i += TAMANO_LOTE) {
            List<MovimientoInventario> lote = movimientos.subList(
                    i, Math.min(i + TAMANO_LOTE, movimientos.size())).stream()
                    .map(this::toEntidad)
                    .toList();
            movimientoRepository.saveAll(lote);
        }
    }

    private MovimientoInventario toEntidad(MovimientoDemo demo) {
        MovimientoInventario movimiento = new MovimientoInventario(
                demo.tipo(),
                demo.productoId(),
                demo.usuarioId(),
                demo.stockAnterior(),
                demo.stockPosterior(),
                demo.variacion(),
                demo.motivo());
        movimiento.setFecha(demo.fecha());
        movimiento.setCantidad(demo.cantidad());
        movimiento.setCostoUnitario(demo.costoUnitario());
        movimiento.setConsumoId(demo.consumoId());
        return movimiento;
    }
}
