package com.taller.inventario.movimiento.service.impl;

import com.taller.catalogo.api.ProductoStock;
import com.taller.catalogo.api.ProductoStock.ResultadoEntrada;
import com.taller.catalogo.api.ProductoStock.ResultadoStock;
import com.taller.inventario.movimiento.dto.request.AjusteRequest;
import com.taller.inventario.movimiento.dto.request.EntradaRequest;
import com.taller.inventario.movimiento.dto.response.EntradaResponse;
import com.taller.inventario.movimiento.dto.response.MovimientoResponse;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import com.taller.inventario.movimiento.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final ProductoStock productoStock;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    @Transactional
    public MovimientoResponse ajuste(AjusteRequest request, Long usuarioId) {
        if (request.cantidadFisica() == null || request.cantidadFisica() < 0) {
            throw new IllegalArgumentException("La cantidad física no puede ser negativa");
        }
        if (request.justificacion() == null || request.justificacion().isBlank()) {
            throw new IllegalArgumentException("La justificación es obligatoria");
        }

        ResultadoStock resultado = productoStock.fijarStock(request.productoId(), request.cantidadFisica());
        int variacion = request.cantidadFisica() - resultado.stockAnterior();

        MovimientoInventario movimiento = new MovimientoInventario(
                TipoMovimiento.AJUSTE,
                request.productoId(),
                usuarioId,
                resultado.stockAnterior(),
                resultado.stockPosterior(),
                variacion,
                request.justificacion().trim());

        return MovimientoResponse.from(movimientoInventarioRepository.save(movimiento));
    }

    @Override
    @Transactional
    public EntradaResponse entrada(EntradaRequest request, Long usuarioId) {
        if (request.cantidad() == null || request.cantidad() <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor que cero");
        }
        if (request.costoUnitario() == null || request.costoUnitario().signum() < 0) {
            throw new IllegalArgumentException("Costo unitario no puede ser negativo");
        }

        ResultadoEntrada resultado = productoStock.aplicarEntrada(
                request.productoId(),
                request.proveedorId(),
                request.cantidad(),
                request.costoUnitario());

        MovimientoInventario movimiento = new MovimientoInventario(
                TipoMovimiento.ENTRADA,
                request.productoId(),
                usuarioId,
                resultado.stockAnterior(),
                resultado.stockPosterior(),
                request.cantidad(),
                resultado.motivo());
        movimiento.setCantidad(request.cantidad());
        movimiento.setCostoUnitario(request.costoUnitario());

        MovimientoInventario guardado = movimientoInventarioRepository.save(movimiento);
        return new EntradaResponse(MovimientoResponse.from(guardado), resultado.producto());
    }
}
