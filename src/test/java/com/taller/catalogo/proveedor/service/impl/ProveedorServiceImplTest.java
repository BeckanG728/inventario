package com.taller.catalogo.proveedor.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taller.catalogo.proveedor.dto.response.ProveedorResponse;
import com.taller.catalogo.proveedor.entity.Proveedor;
import com.taller.catalogo.proveedor.repository.ProveedorRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/** Listado paginado de proveedores. */
@ExtendWith(MockitoExtension.class)
class ProveedorServiceImplTest {

    @Mock
    private ProveedorRepository repository;

    @InjectMocks
    private ProveedorServiceImpl service;

    @Test
    @SuppressWarnings("unchecked")
    void listar_mapeaLaPagina() {
        Proveedor proveedor = new Proveedor("20123456789", "Distribuidor Los Pinos", null, null, null, null);
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(proveedor), PageRequest.of(0, 10), 1));

        Page<ProveedorResponse> pagina = service.listar(0, 10, "pinos", "2012", true);

        assertEquals(1, pagina.getTotalElements());
        assertEquals("20123456789", pagina.getContent().get(0).ruc());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listar_tamanoYPaginaFueraDeRango_seAjustan() {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.listar(-1, 1000, null, null, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }
}