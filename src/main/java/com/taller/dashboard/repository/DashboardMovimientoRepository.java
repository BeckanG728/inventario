package com.taller.dashboard.repository;

import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import java.time.Instant;
import java.util.List;

public interface DashboardMovimientoRepository extends Repository<MovimientoInventario, Long> {

    @EntityGraph(attributePaths = {"producto", "usuario"})
    List<MovimientoInventario> findTop5ByOrderByFechaDescIdDesc();

    long countByTipoAndFechaGreaterThanEqual(TipoMovimiento tipo, Instant desde);
}
