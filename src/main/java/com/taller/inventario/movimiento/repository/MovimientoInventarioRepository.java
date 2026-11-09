package com.taller.inventario.movimiento.repository;

import com.taller.inventario.movimiento.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    boolean existsByProductoId(Long productoId);
}
