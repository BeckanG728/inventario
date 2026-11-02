package com.taller.reporte.repository;

import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MovimientoReporteRepository extends Repository<MovimientoInventario, Long> {

    /** Solo las fechas del rango [start, end). */
    @Query("select m.fecha from MovimientoInventario m where m.fecha >= :start and m.fecha < :end")
    List<Instant> findFechasBetween(@Param("start") Instant start, @Param("end") Instant end);

    /** Movimientos del rango [start, end), del más reciente al más antiguo. */
    @Query("select m from MovimientoInventario m "
            + "where m.fecha >= :start and m.fecha < :end "
            + "order by m.fecha desc, m.id desc")
    List<MovimientoInventario> findMovimientosBetween(@Param("start") Instant start,
                                                      @Param("end") Instant end);

    /** Movimientos del rango [start, end) de un tipo, del más reciente al más antiguo. */
    @Query("select m from MovimientoInventario m "
            + "where m.fecha >= :start and m.fecha < :end and m.tipo = :tipo "
            + "order by m.fecha desc, m.id desc")
    List<MovimientoInventario> findMovimientosBetweenAndTipo(@Param("start") Instant start,
                                                             @Param("end") Instant end,
                                                             @Param("tipo") TipoMovimiento tipo);

    /** Consumos del rango [start, end) con producto y usuario cargados para el reporte. */
    @Query("select m from MovimientoInventario m "
            + "join fetch m.producto join fetch m.usuario "
            + "where m.fecha >= :start and m.fecha < :end and m.tipo = :tipo "
            + "order by m.fecha asc, m.id asc")
    List<MovimientoInventario> findMovimientosConDetalleBetweenAndTipo(@Param("start") Instant start,
                                                                       @Param("end") Instant end,
                                                                       @Param("tipo") TipoMovimiento tipo);
}
