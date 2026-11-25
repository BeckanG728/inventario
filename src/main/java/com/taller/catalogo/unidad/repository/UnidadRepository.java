package com.taller.catalogo.unidad.repository;

import com.taller.catalogo.unidad.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UnidadRepository extends JpaRepository<Unidad, Long> {

    Optional<Unidad> findByCodigoIgnoreCase(String codigo);

    List<Unidad> findByActivoTrueOrderByNombreAsc();

    boolean existsByIdAndActivoTrue(Long id);

    @Query(value = """
            select u.* from unidad u
            join categoria_unidad cu on cu.unidad_id = u.id
            where cu.categoria_id = :categoriaId and u.activo = true
            order by u.nombre asc
            """, nativeQuery = true)
    List<Unidad> findActivasPorCategoria(@Param("categoriaId") Long categoriaId);

    @Query(value = """
            select count(*) > 0 from producto p
            where p.unidad_base_id = :unidadId or p.unidad_presentacion_id = :unidadId
            """, nativeQuery = true)
    boolean esUnidadEnUso(@Param("unidadId") Long unidadId);
}
