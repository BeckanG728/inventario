package com.taller.seguridad.usuario.repository;

import com.taller.seguridad.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>,
        JpaSpecificationExecutor<Usuario> {

    boolean existsByUsername(String username);

    Optional<Usuario> findByUsername(String username);

    @Query(value = "select nombre from bloqueo_operacion where nombre = 'administradores' for update", nativeQuery = true)
    String bloquearCambiosAdministrativos();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);

    long countByActivoTrueAndRolNombre(String rolNombre);
}
