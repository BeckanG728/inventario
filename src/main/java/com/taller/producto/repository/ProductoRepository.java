package com.taller.producto.repository;

import com.taller.producto.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long>,
        JpaSpecificationExecutor<Producto> {

    @Override
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    Page<Producto> findAll(Specification<Producto> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    List<Producto> findAllByActivoTrue();

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);
}
