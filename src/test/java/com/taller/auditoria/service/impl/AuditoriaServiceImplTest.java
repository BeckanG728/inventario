package com.taller.auditoria.service.impl;

import com.taller.auditoria.domain.AuditoriaOperacion;
import com.taller.auditoria.dto.response.AuditoriaResponse;
import com.taller.auditoria.enums.AuditoriaTipo;
import com.taller.auditoria.mapper.AuditoriaMapper;
import com.taller.auditoria.repository.AuditoriaOperacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceImplTest {

    @Mock
    private AuditoriaOperacionRepository repository;

    @Mock
    private AuditoriaMapper mapper;

    @InjectMocks
    private AuditoriaServiceImpl service;

    @Test
    void listar_mapeaLaPaginaYOrdenaPorFechaDescendente() {
        AuditoriaOperacion entidad = new AuditoriaOperacion(
                Instant.parse("2026-11-19T17:00:00Z"), AuditoriaTipo.OPERACION, 1L, "juan",
                "POST", "/api/productos", "producto", null, "OK", true);
        AuditoriaResponse respuesta = new AuditoriaResponse(
                1L, entidad.getFecha(), AuditoriaTipo.OPERACION, 1L, "juan",
                "POST", "/api/productos", "producto", null, "OK", true);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entidad)));
        when(mapper.toResponse(entidad)).thenReturn(respuesta);

        Page<AuditoriaResponse> pagina = service.listar(0, 10,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 19),
                "juan", "OPERACION", "OK");

        assertEquals(1, pagina.getTotalElements());
        assertEquals(respuesta, pagina.getContent().get(0));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("fecha").isDescending());
        assertTrue(pageable.getSort().getOrderFor("id").isDescending());
    }

    @Test
    void listar_rangoInvalido_lanzaErrorSinConsultar() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.listar(0, 10, LocalDate.of(2026, 11, 20),
                        LocalDate.of(2026, 11, 19), null, null, null));

        assertEquals("La fecha final no puede ser anterior a la inicial", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void listar_tipoInvalido_lanzaError() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.listar(0, 10, null, null, null, "NO_EXISTE", null));

        assertEquals("Tipo de operación inválido: NO_EXISTE", ex.getMessage());
    }

    @Test
    void listar_tamanoFueraDeRango_seAjusta() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.listar(-5, 500, null, null, null, null, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }
}
