package com.taller.inventario.api;

import com.taller.inventario.movimiento.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Consultas públicas del historial de movimientos de inventario. */
public interface HistorialMovimientos {

    boolean existePorProducto(Long productoId);

    List<MovimientoDetalle> consultarEntre(Instant desde, Instant hasta, TipoMovimiento tipo);

    Page<MovimientoDetalle> consultarPagina(Instant desde, Instant hasta, TipoMovimiento tipo,
                                            Pageable pageable);

    Map<TipoMovimiento, Long> contarPorTipoEntre(Instant desde, Instant hasta);

    List<Instant> consultarFechasEntre(Instant desde, Instant hasta);

    /** Movimientos de tipo CONSUMO en el rango, ordenados de más antiguo a más reciente. */
    List<MovimientoDetalle> consultarConsumosEntre(Instant desde, Instant hasta);

    /** Los cinco movimientos más recientes, usados por el dashboard. */
    List<MovimientoDetalle> consultarUltimos();

    long contarPorTipoYFechaDesde(TipoMovimiento tipo, Instant desde);
}
