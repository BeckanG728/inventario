package com.taller.reporte.repository;

import com.taller.producto.entity.Producto;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface ProductoReporteRepository extends Repository<Producto, Long> {

    List<Producto> findByActivoTrueOrderByNombreAsc();
}