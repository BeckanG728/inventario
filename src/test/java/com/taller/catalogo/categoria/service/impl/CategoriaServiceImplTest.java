package com.taller.catalogo.categoria.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taller.catalogo.categoria.dto.request.CategoriaRequest;
import com.taller.catalogo.categoria.dto.response.CategoriaResponse;
import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.categoria.repository.CategoriaRepository;
import com.taller.catalogo.unidad.entity.Unidad;
import com.taller.catalogo.unidad.repository.UnidadRepository;
import java.util.List;
import java.util.Optional;
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

/** Alta y listado paginado de categorías. */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository repository;

    @Mock
    private UnidadRepository unidadRepository;

    @InjectMocks
    private CategoriaServiceImpl service;

    @Test
    void crear_categoriaNueva_recibeSoloUND() {
        Unidad und = new Unidad("UND", "Unidad", false);
        when(repository.existsByNombreIgnoreCase("Cables")).thenReturn(false);
        when(unidadRepository.findByCodigoIgnoreCase("UND")).thenReturn(Optional.of(und));
        when(repository.save(any(Categoria.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        CategoriaResponse respuesta = service.crear(new CategoriaRequest("Cables", null));

        assertEquals(1, respuesta.unidades().size());
        assertEquals("UND", respuesta.unidades().get(0).codigo());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listar_mapeaLaPagina() {
        Categoria categoria = new Categoria("Filtros", "Filtros de aceite");
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(categoria), PageRequest.of(0, 10), 1));

        Page<CategoriaResponse> pagina = service.listar(0, 10, "fil", true);

        assertEquals(1, pagina.getTotalElements());
        assertEquals("Filtros", pagina.getContent().get(0).nombre());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listar_tamanoYPaginaFueraDeRango_seAjustan() {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.listar(-5, 500, null, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listar_tamanoCero_seAjustaAUno() {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.listar(0, 0, null, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        assertEquals(1, captor.getValue().getPageSize());
    }
}