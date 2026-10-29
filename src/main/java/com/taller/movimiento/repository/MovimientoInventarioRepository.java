package com.taller.movimiento.repository;

import com.taller.movimiento.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    boolean existsByProductoId(Long productoId);
}
