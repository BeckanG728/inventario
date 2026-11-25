package com.taller.catalogo.categoria.repository;

import com.taller.catalogo.categoria.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long>,
        JpaSpecificationExecutor<Categoria> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}