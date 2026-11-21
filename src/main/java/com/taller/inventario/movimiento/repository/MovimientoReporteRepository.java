package com.taller.inventario.movimiento.repository;

import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Repositorio de lectura de inventario para los reportes. Se mantiene separado del de dashboard. */
public interface MovimientoReporteRepository extends Repository<MovimientoInventario, Long> {

    /** Total de movimientos por tipo en el rango [start, end). */
    interface TotalPorTipo {
        TipoMovimiento getTipo();

        long getTotal();
    }

    @Query("select m.fecha from MovimientoInventario m where m.fecha >= :start and m.fecha < :end")
    List<Instant> findFechasBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("select m from MovimientoInventario m "
            + "where m.fecha >= :start and m.fecha < :end "
            + "order by m.fecha desc, m.id desc")
    List<MovimientoInventario> findMovimientosBetween(@Param("start") Instant start,
                                                      @Param("end") Instant end);

    @Query("select m from MovimientoInventario m "
            + "where m.fecha >= :start and m.fecha < :end and m.tipo = :tipo "
            + "order by m.fecha desc, m.id desc")
    List<MovimientoInventario> findMovimientosBetweenAndTipo(@Param("start") Instant start,
                                                             @Param("end") Instant end,
                                                             @Param("tipo") TipoMovimiento tipo);

    @Query("select m from MovimientoInventario m "
            + "where m.fecha >= :start and m.fecha < :end "
            + "order by m.fecha desc, m.id desc")
    Page<MovimientoInventario> findPaginaBetween(@Param("start") Instant start,
                                                 @Param("end") Instant end,
                                                 Pageable pageable);

    @Query("select m from MovimientoInventario m "
            + "where m.fecha >= :start and m.fecha < :end and m.tipo = :tipo "
            + "order by m.fecha desc, m.id desc")
    Page<MovimientoInventario> findPaginaBetweenAndTipo(@Param("start") Instant start,
                                                        @Param("end") Instant end,
                                                        @Param("tipo") TipoMovimiento tipo,
                                                        Pageable pageable);

    @Query("select m.tipo as tipo, count(m) as total from MovimientoInventario m "
            + "where m.fecha >= :start and m.fecha < :end group by m.tipo")
    List<TotalPorTipo> contarPorTipoBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("select m from MovimientoInventario m "
            + "where m.fecha >= :start and m.fecha < :end and m.tipo = :tipo "
            + "order by m.fecha asc, m.id asc")
    List<MovimientoInventario> findMovimientosConDetalleBetweenAndTipo(@Param("start") Instant start,
                                                                       @Param("end") Instant end,
                                                                       @Param("tipo") TipoMovimiento tipo);
}
