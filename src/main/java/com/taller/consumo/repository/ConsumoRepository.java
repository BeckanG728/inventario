package com.taller.consumo.repository;

import com.taller.consumo.entity.Consumo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {
}
