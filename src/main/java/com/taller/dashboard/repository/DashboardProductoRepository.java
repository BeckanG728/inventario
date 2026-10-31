package com.taller.dashboard.repository;

import com.taller.producto.entity.Producto;
import org.springframework.data.repository.Repository;

public interface DashboardProductoRepository extends Repository<Producto, Long> {

    long countByActivoTrue();
}