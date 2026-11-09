package com.taller.catalogo.proveedor.repository;

import com.taller.catalogo.proveedor.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    boolean existsByRuc(String ruc);

    Optional<Proveedor> findByIdAndActivoTrue(Long id);
}