package com.taller.catalogo.proveedor.repository;

import com.taller.catalogo.proveedor.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long>,
        JpaSpecificationExecutor<Proveedor> {

    boolean existsByRuc(String ruc);

    Optional<Proveedor> findByIdAndActivoTrue(Long id);
}