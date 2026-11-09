package com.taller.inventario.consumo.repository;

import com.taller.inventario.consumo.entity.Consumo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {
}
