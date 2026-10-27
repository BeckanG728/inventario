package com.taller.sec.repository;

import com.taller.sec.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<Usuario> findByEmail(String email);
}