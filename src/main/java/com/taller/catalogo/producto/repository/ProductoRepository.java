package com.taller.catalogo.producto.repository;

import com.taller.catalogo.producto.entity.Producto;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long>,
        JpaSpecificationExecutor<Producto> {

    @Override
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    Page<Producto> findAll(Specification<Producto> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    List<Producto> findAllByActivoTrue();

    @Override
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    List<Producto> findAllById(Iterable<Long> ids);

    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    List<Producto> findByActivoTrueOrderByNombreAsc();

    /** Bloquea todos los productos en orden ascendente de id para evitar interbloqueos entre consumos. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"categoria", "proveedor"})
    @Query("select p from Producto p where p.id in :ids and p.activo = true order by p.id")
    List<Producto> findAllByIdInAndActivoTrueForUpdate(@Param("ids") Collection<Long> ids);

    long countByActivoTrue();

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    /** Código con correlativo más alto de un prefijo; null si no hay ninguno. */
    @Query(value = """
            select codigo from producto where codigo like concat(:prefijo, '-%')
            and codigo ~ '-[0-9]+$' order by length(codigo) desc, codigo desc limit 1
            """, nativeQuery = true)
    String findMaxCodigoByPrefijo(@Param("prefijo") String prefijo);

    Optional<Producto> findByIdAndActivoTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    @Query(value = "select nombre from bloqueo_operacion where nombre = 'alta_producto' for update", nativeQuery = true)
    String bloquearAlta();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id and p.activo = true")
    Optional<Producto> findByIdAndActivoTrueForUpdate(@Param("id") Long id);
}
