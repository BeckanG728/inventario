package com.taller.inventario.movimiento.repository;

import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import org.springframework.data.repository.Repository;

import java.time.Instant;
import java.util.List;

/** Repositorio de lectura de inventario para el dashboard. No se unifica con el de reportes. */
public interface DashboardMovimientoRepository extends Repository<MovimientoInventario, Long> {

    List<MovimientoInventario> findTop5ByOrderByFechaDescIdDesc();

    long countByTipoAndFechaGreaterThanEqual(TipoMovimiento tipo, Instant desde);
}
