package com.taller.auditoria.repository;

import com.taller.auditoria.domain.AuditoriaOperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditoriaOperacionRepository
        extends JpaRepository<AuditoriaOperacion, Long>, JpaSpecificationExecutor<AuditoriaOperacion> {
}
