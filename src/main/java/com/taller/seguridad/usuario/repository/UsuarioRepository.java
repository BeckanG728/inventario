package com.taller.seguridad.usuario.repository;

import com.taller.seguridad.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>,
        JpaSpecificationExecutor<Usuario> {

    boolean existsByUsername(String username);

    Optional<Usuario> findByUsername(String username);

    long countByActivoTrueAndRolNombre(String rolNombre);
}
