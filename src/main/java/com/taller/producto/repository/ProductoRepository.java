package com.taller.producto.repository;

import com.taller.producto.entity.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long>,
        JpaSpecificationExecutor<Producto> {

    @Override
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    Page<Producto> findAll(Specification<Producto> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    List<Producto> findAllByActivoTrue();

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    Optional<Producto> findByIdAndActivoTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id and p.activo = true")
    Optional<Producto> findByIdAndActivoTrueForUpdate(@Param("id") Long id);
}
