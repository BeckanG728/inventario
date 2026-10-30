package com.taller.movimiento.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.movimiento.dto.request.AjusteRequest;
import com.taller.movimiento.dto.response.MovimientoResponse;
import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.movimiento.repository.MovimientoInventarioRepository;
import com.taller.movimiento.service.MovimientoService;
import com.taller.producto.entity.Producto;
import com.taller.producto.exception.ProductoNotFoundException;
import com.taller.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

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
}
