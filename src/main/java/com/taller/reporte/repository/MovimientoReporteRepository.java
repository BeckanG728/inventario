package com.taller.reporte.repository;

import com.taller.movimiento.entity.MovimientoInventario;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MovimientoReporteRepository extends Repository<MovimientoInventario, Long> {

    /** Solo las fechas del rango [start, end). */
    @Query("select m.fecha from MovimientoInventario m where m.fecha >= :start and m.fecha < :end")
    List<Instant> findFechasBetween(@Param("start") Instant start, @Param("end") Instant end);
}