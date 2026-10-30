package com.taller.movimiento.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.catalogo.entity.Proveedor;
import com.taller.catalogo.repository.ProveedorRepository;
import com.taller.movimiento.dto.request.AjusteRequest;
import com.taller.movimiento.dto.request.EntradaRequest;
import com.taller.movimiento.dto.response.EntradaResponse;
import com.taller.movimiento.dto.response.MovimientoResponse;
import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.movimiento.repository.MovimientoInventarioRepository;
import com.taller.movimiento.service.MovimientoService;
import com.taller.producto.entity.Producto;
import com.taller.producto.exception.ProductoNotFoundException;
import com.taller.producto.mapper.ProductoMapper;
import com.taller.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional
    public MovimientoResponse ajuste(AjusteRequest request, Usuario usuario) {
        if (request.cantidadFisica() == null || request.cantidadFisica() < 0) {
            throw new IllegalArgumentException("La cantidad física no puede ser negativa");
        }
        if (request.justificacion() == null || request.justificacion().isBlank()) {
            throw new IllegalArgumentException("La justificación es obligatoria");
        }

        Producto producto = productoRepository.findByIdAndActivoTrueForUpdate(request.productoId())
                .orElseThrow(() -> new ProductoNotFoundException(request.productoId()));

        int stockAnterior = producto.getStockActual();
        int variacion = request.cantidadFisica() - stockAnterior;
        producto.setStockActual(request.cantidadFisica());

        MovimientoInventario movimiento = new MovimientoInventario(
                TipoMovimiento.AJUSTE,
                producto,
                usuario,
                stockAnterior,
                request.cantidadFisica(),
                variacion,
                request.justificacion().trim());

        return MovimientoResponse.from(movimientoInventarioRepository.save(movimiento));
    }

    @Override
    @Transactional
    public EntradaResponse entrada(EntradaRequest request, Usuario usuario) {
        if (request.cantidad() == null || request.cantidad() <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor que cero");
        }
        if (request.costoUnitario() == null || request.costoUnitario().signum() < 0) {
            throw new IllegalArgumentException("Costo unitario no puede ser negativo");
        }

        Producto producto = productoRepository.findByIdAndActivoTrueForUpdate(request.productoId())
                .orElseThrow(() -> new ProductoNotFoundException(request.productoId()));
        Proveedor proveedor = proveedorRepository.findByIdAndActivoTrue(request.proveedorId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado o inactivo"));

        int stockAnterior = producto.getStockActual();
        int stockPosterior;
        try {
            stockPosterior = Math.addExact(stockAnterior, request.cantidad());
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("La cantidad excede el stock máximo permitido");
        }

        producto.setStockActual(stockPosterior);
        producto.setCostoUltimaCompra(request.costoUnitario());

        MovimientoInventario movimiento = new MovimientoInventario(
                TipoMovimiento.ENTRADA,
                producto,
                usuario,
                stockAnterior,
                stockPosterior,
                request.cantidad(),
                "Entrada de proveedor " + proveedor.getRazonSocial());
        movimiento.setCantidad(request.cantidad());
        movimiento.setCostoUnitario(request.costoUnitario());

        MovimientoInventario guardado = movimientoInventarioRepository.save(movimiento);
        return new EntradaResponse(MovimientoResponse.from(guardado), productoMapper.toResponse(producto));
    }
}
