package com.taller.inventario.api.impl;

import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.inventario.api.HistorialMovimientos;
import com.taller.inventario.api.MovimientoDetalle;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.repository.DashboardMovimientoRepository;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import com.taller.inventario.movimiento.repository.MovimientoReporteRepository;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.seguridad.api.UsuarioConsulta;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HistorialMovimientosImpl implements HistorialMovimientos {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final MovimientoReporteRepository movimientoReporteRepository;
    private final DashboardMovimientoRepository dashboardMovimientoRepository;
    private final ProductoStockMinimo productoStockMinimo;
    private final UsuarioConsulta usuarioConsulta;

    @Override
    @Transactional(readOnly = true)
    public boolean existePorProducto(Long productoId) {
        return movimientoInventarioRepository.existsByProductoId(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDetalle> consultarEntre(Instant desde, Instant hasta, TipoMovimiento tipo) {
        List<MovimientoInventario> movimientos = tipo == null
                ? movimientoReporteRepository.findMovimientosBetween(desde, hasta)
                : movimientoReporteRepository.findMovimientosBetweenAndTipo(desde, hasta, tipo);
        return aDetalle(movimientos);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoDetalle> consultarPagina(Instant desde, Instant hasta, TipoMovimiento tipo,
                                                   Pageable pageable) {
        Page<MovimientoInventario> pagina = tipo == null
                ? movimientoReporteRepository.findPaginaBetween(desde, hasta, pageable)
                : movimientoReporteRepository.findPaginaBetweenAndTipo(desde, hasta, tipo, pageable);
        return new PageImpl<>(aDetalle(pagina.getContent()), pageable, pagina.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<TipoMovimiento, Long> contarPorTipoEntre(Instant desde, Instant hasta) {
        return movimientoReporteRepository.contarPorTipoBetween(desde, hasta).stream()
                .collect(Collectors.toMap(
                        MovimientoReporteRepository.TotalPorTipo::getTipo,
                        MovimientoReporteRepository.TotalPorTipo::getTotal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Instant> consultarFechasEntre(Instant desde, Instant hasta) {
        return movimientoReporteRepository.findFechasBetween(desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FechaTipo> consultarFechaTipoEntre(Instant desde, Instant hasta, Long productoId) {
        List<MovimientoReporteRepository.FechaTipo> filas = productoId == null
                ? movimientoReporteRepository.findFechaTipoBetween(desde, hasta)
                : movimientoReporteRepository.findFechaTipoBetweenAndProducto(desde, hasta, productoId);
        return filas.stream()
                .map(f -> new FechaTipo(f.getFecha(), f.getTipo(), f.getCantidad(), f.getVariacion(),
                        f.getCostoUnitario()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDetalle> consultarConsumosEntre(Instant desde, Instant hasta) {
        return aDetalle(movimientoReporteRepository.findMovimientosConDetalleBetweenAndTipo(
                desde, hasta, TipoMovimiento.CONSUMO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDetalle> consultarUltimos() {
        return aDetalle(dashboardMovimientoRepository.findTop5ByOrderByFechaDescIdDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorTipoYFechaDesde(TipoMovimiento tipo, Instant desde) {
        return dashboardMovimientoRepository.countByTipoAndFechaGreaterThanEqual(tipo, desde);
    }

    private List<MovimientoDetalle> aDetalle(List<MovimientoInventario> movimientos) {
        Set<Long> productoIds = new LinkedHashSet<>();
        Set<Long> usuarioIds = new LinkedHashSet<>();
        for (MovimientoInventario movimiento : movimientos) {
            productoIds.add(movimiento.getProductoId());
            usuarioIds.add(movimiento.getUsuarioId());
        }

        Map<Long, ProductoResponse> productos = productoStockMinimo.consultarPorIds(new ArrayList<>(productoIds))
                .stream()
                .collect(Collectors.toMap(ProductoResponse::id, Function.identity()));
        Map<Long, String> usuarios = usuarioConsulta.consultarUsuariosPorIds(new ArrayList<>(usuarioIds));

        return movimientos.stream()
                .map(m -> toDetalle(m, productos.get(m.getProductoId()), usuarios.get(m.getUsuarioId())))
                .toList();
    }

    private MovimientoDetalle toDetalle(MovimientoInventario m, ProductoResponse producto, String username) {
        return new MovimientoDetalle(
                m.getId(),
                m.getFecha(),
                m.getTipo(),
                m.getCantidad(),
                m.getVariacion(),
                m.getStockAnterior(),
                m.getStockPosterior(),
                m.getCostoUnitario(),
                m.getMotivo(),
                m.getProductoId(),
                producto == null ? null : producto.codigo(),
                producto == null ? null : producto.nombre(),
                m.getConsumoId(),
                m.getUsuarioId(),
                username,
                m.getUnidadRegistro(),
                m.getFactorAplicado(),
                m.getCantidadBase());
    }
}
