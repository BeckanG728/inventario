package com.taller.inventario.movimiento.service.impl;

import com.taller.catalogo.api.ProductoStock;
import com.taller.catalogo.api.ProductoStock.ResultadoEntrada;
import com.taller.catalogo.api.ProductoStock.ResultadoStock;
import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.unidad.service.ConversorUnidades;
import com.taller.catalogo.unidad.service.ConversorUnidades.ResultadoConversion;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final ProductoStock productoStock;
    private final ProductoStockMinimo productoStockMinimo;
    private final ConversorUnidades conversorUnidades;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    @Transactional
    public MovimientoResponse ajuste(AjusteRequest request, Long usuarioId) {
        if ((request.cantidadFisica() == null || request.cantidadFisica() < 0)
                && (request.cantidadFisicaExacta() == null || request.cantidadFisicaExacta().signum() < 0)) {
            throw new IllegalArgumentException("La cantidad física no puede ser negativa");
        }
        if (request.justificacion() == null || request.justificacion().isBlank()) {
            throw new IllegalArgumentException("La justificación es obligatoria");
        }

        ProductoResponse producto = obtenerProducto(request.productoId());
        ResultadoConversion conversion = conversorUnidades.convertir(
                producto,
                request.cantidadFisica(),
                request.cantidadFisicaExacta(),
                request.unidadRegistro(),
                request.factorAplicado(),
                null);

        ResultadoStock resultado = productoStock.fijarStock(request.productoId(), conversion.cantidadBase());
        int variacion = conversion.cantidadBase() - resultado.stockAnterior();

        MovimientoInventario movimiento = new MovimientoInventario(
                TipoMovimiento.AJUSTE,
                request.productoId(),
                usuarioId,
                resultado.stockAnterior(),
                resultado.stockPosterior(),
                variacion,
                request.justificacion().trim());

        // RN208: se conserva el costo vigente también en los ajustes, para trazabilidad.
        movimiento.setCostoUnitario(resultado.producto().costoUltimaCompra());
        movimiento.setUnidadRegistro(conversion.unidadRegistro());
        movimiento.setFactorAplicado(conversion.factorAplicado());
        // RN209: el ajuste no tiene "cantidad registrada", solo fija el stock en unidad base.
        return MovimientoResponse.from(movimientoInventarioRepository.save(movimiento));
    }

    @Override
    @Transactional
    public EntradaResponse entrada(EntradaRequest request, Long usuarioId) {
        if (request.costoUnitario() == null || request.costoUnitario().signum() < 0) {
            throw new IllegalArgumentException("Costo unitario no puede ser negativo");
        }

        ProductoResponse producto = obtenerProducto(request.productoId());
        ResultadoConversion conversion = conversorUnidades.convertir(
                producto,
                request.cantidad(),
                request.cantidadExacta(),
                request.unidadRegistro(),
                request.factorAplicado(),
                request.costoUnitario());

        ResultadoEntrada resultado = productoStock.aplicarEntrada(
                request.productoId(),
                request.proveedorId(),
                conversion.cantidadBase(),
                conversion.costoUnitarioBase());

        MovimientoInventario movimiento = new MovimientoInventario(
                TipoMovimiento.ENTRADA,
                request.productoId(),
                usuarioId,
                resultado.stockAnterior(),
                resultado.stockPosterior(),
                conversion.cantidadBase(),
                resultado.motivo());
        movimiento.setCantidad(conversion.cantidadBase());
        movimiento.setCantidadBase(conversion.cantidadBase());
        movimiento.setUnidadRegistro(conversion.unidadRegistro());
        movimiento.setFactorAplicado(conversion.factorAplicado());
        movimiento.setCostoUnitario(conversion.costoUnitarioBase());

        MovimientoInventario guardado = movimientoInventarioRepository.save(movimiento);
        return new EntradaResponse(MovimientoResponse.from(guardado), resultado.producto());
    }

    private ProductoResponse obtenerProducto(Long productoId) {
        if (productoId == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        return productoStockMinimo.consultarPorIds(List.of(productoId)).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
    }
}
