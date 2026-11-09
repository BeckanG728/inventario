package com.taller.reporte.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.producto.entity.Producto;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.dto.response.ReporteMovimientosPaginaResponse;
import com.taller.reporte.repository.MovimientoReporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

/**
 * US-17 — Reporte de movimientos por periodo. Cubre RN405 (validación de rango de fechas)
 * y RN408 (solo lectura, movimientos históricos aunque el producto esté inactivo).
 */
@ExtendWith(MockitoExtension.class)
class ReporteMovimientosServiceImplTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Instant INICIO_ENERO = LocalDate.of(2026, 1, 1).atStartOfDay(ZONE).toInstant();
    private static final Instant FIN_ENERO_EXCLUSIVO = LocalDate.of(2026, 2, 1).atStartOfDay(ZONE).toInstant();

    @Mock
    private MovimientoReporteRepository repository;

    @InjectMocks
    private ReporteMovimientosServiceImpl service;

    private MovimientoInventario movimiento(long id, TipoMovimiento tipo, boolean productoActivo) {
        Producto producto = new Producto("P-1", "Filtro de aceite", "UN", 1, null, null);
        producto.setId(1L);
        producto.setActivo(productoActivo);
        Usuario usuario = new Usuario("mecanico", "pwd", null);
        usuario.setId(2L);
        MovimientoInventario m = new MovimientoInventario(tipo, producto, usuario, 10, 5, -5, "motivo");
        m.setId(id);
        return m;
    }

    // ---------- RN405: validación del rango de fechas ----------

    @Test
    void consultar_sinDesde_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consultar(null, LocalDate.of(2026, 1, 31), null));
        verifyNoInteractions(repository);
    }

    @Test
    void consultar_sinHasta_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consultar(LocalDate.of(2026, 1, 1), null, null));
        verifyNoInteractions(repository);
    }

    @Test
    void consultar_hastaAnteriorADesde_lanzaErrorConMensajeEsperado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.consultar(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31), null));
        assertEquals("La fecha final no puede ser anterior a la inicial", ex.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void consultar_desdeIgualAHasta_esValidoEIncluyeElDiaCompleto() {
        LocalDate dia = LocalDate.of(2026, 1, 15);
        when(repository.findMovimientosBetween(
                LocalDate.of(2026, 1, 15).atStartOfDay(ZONE).toInstant(),
                LocalDate.of(2026, 1, 16).atStartOfDay(ZONE).toInstant()))
                .thenReturn(List.of());

        ReporteMovimientosResponse response = service.consultar(dia, dia, null);

        assertTrue(response.movimientos().isEmpty());
    }

    @Test
    void consultar_hastaEsInclusivo_usaLimiteSuperiorExclusivoAlDiaSiguiente() {
        when(repository.findMovimientosBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of());

        service.consultar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        verify(repository).findMovimientosBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO);
    }

    // ---------- Filtro por tipo y totales ----------

    @Test
    void consultar_sinTipo_noUsaLaConsultaFiltrada() {
        when(repository.findMovimientosBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of());

        service.consultar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        verify(repository, never()).findMovimientosBetweenAndTipo(
                INICIO_ENERO, FIN_ENERO_EXCLUSIVO, TipoMovimiento.ENTRADA);
    }

    @Test
    void consultar_conTipo_usaLaConsultaFiltradaPorTipo() {
        when(repository.findMovimientosBetweenAndTipo(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, TipoMovimiento.CONSUMO))
                .thenReturn(List.of(movimiento(1, TipoMovimiento.CONSUMO, true)));

        ReporteMovimientosResponse response = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), TipoMovimiento.CONSUMO);

        assertEquals(0, response.totalEntradas());
        assertEquals(1, response.totalConsumos());
        assertEquals(0, response.totalAjustes());
        assertEquals(1, response.movimientos().size());
        verify(repository, never()).findMovimientosBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO);
    }

    @Test
    void consultar_calculaTotalesPorTipo() {
        when(repository.findMovimientosBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                movimiento(1, TipoMovimiento.ENTRADA, true),
                movimiento(2, TipoMovimiento.ENTRADA, true),
                movimiento(3, TipoMovimiento.CONSUMO, true),
                movimiento(4, TipoMovimiento.AJUSTE, true),
                movimiento(5, TipoMovimiento.AJUSTE, true),
                movimiento(6, TipoMovimiento.AJUSTE, true)));

        ReporteMovimientosResponse response = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        assertEquals(2, response.totalEntradas());
        assertEquals(1, response.totalConsumos());
        assertEquals(3, response.totalAjustes());
        assertEquals(6, response.movimientos().size());
    }

    // ---------- RN408: histórico y solo lectura ----------

    @Test
    void consultar_incluyeMovimientosDeProductosInactivos() {
        when(repository.findMovimientosBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                movimiento(1, TipoMovimiento.ENTRADA, false),
                movimiento(2, TipoMovimiento.CONSUMO, true)));

        ReporteMovimientosResponse response = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        assertEquals(2, response.movimientos().size());
        assertEquals(1L, response.movimientos().get(0).productoId());
        assertEquals(1, response.totalEntradas());
    }

    @Test
    void consultar_sinMovimientos_devuelveTotalesEnCeroYListaVacia() {
        when(repository.findMovimientosBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of());

        ReporteMovimientosResponse response = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        assertEquals(0, response.totalEntradas());
        assertEquals(0, response.totalConsumos());
        assertEquals(0, response.totalAjustes());
        assertTrue(response.movimientos().isEmpty());
    }

    // ---------- Paginación (CATALOGO_ENDPOINTS §9.1, M400-07) ----------

    private MovimientoReporteRepository.TotalPorTipo total(TipoMovimiento tipo, long total) {
        return new MovimientoReporteRepository.TotalPorTipo() {
            @Override
            public TipoMovimiento getTipo() {
                return tipo;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }

    @Test
    void consultarPagina_sinDesde_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consultarPagina(null, LocalDate.of(2026, 1, 31), null, 0, 10));
        verifyNoInteractions(repository);
    }

    @Test
    void consultarPagina_sinTipo_devuelvePaginaYTotalesDeTodoElFiltro() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 2);
        when(repository.findPaginaBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, pageable)).thenReturn(new PageImpl<>(
                List.of(movimiento(1, TipoMovimiento.ENTRADA, true), movimiento(2, TipoMovimiento.CONSUMO, true)),
                pageable, 6));
        when(repository.contarPorTipoBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                total(TipoMovimiento.ENTRADA, 2), total(TipoMovimiento.CONSUMO, 1), total(TipoMovimiento.AJUSTE, 3)));

        ReporteMovimientosPaginaResponse response = service.consultarPagina(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 0, 2);

        assertEquals(2, response.movimientos().size());
        assertEquals(2, response.totalEntradas());
        assertEquals(1, response.totalConsumos());
        assertEquals(3, response.totalAjustes());
        assertEquals(0, response.page());
        assertEquals(2, response.size());
        assertEquals(6, response.totalElements());
        assertEquals(3, response.totalPages());
    }

    @Test
    void consultarPagina_conTipo_usaConsultaFiltradaYPonePorEnCeroLosOtrosTotales() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        when(repository.findPaginaBetweenAndTipo(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, TipoMovimiento.ENTRADA, pageable))
                .thenReturn(new PageImpl<>(List.of(
                        movimiento(1, TipoMovimiento.ENTRADA, true),
                        movimiento(2, TipoMovimiento.ENTRADA, true)), pageable, 2));
        when(repository.contarPorTipoBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                total(TipoMovimiento.ENTRADA, 2), total(TipoMovimiento.CONSUMO, 1)));

        ReporteMovimientosPaginaResponse response = service.consultarPagina(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), TipoMovimiento.ENTRADA, 0, 10);

        assertEquals(2, response.totalEntradas());
        assertEquals(0, response.totalConsumos());
        assertEquals(0, response.totalAjustes());
        assertEquals(2, response.totalElements());
        verify(repository, never()).findPaginaBetween(any(), any(), any());
    }

    @Test
    void consultarPagina_normalizaPageYSize() {
        when(repository.findPaginaBetween(any(), any(), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<MovimientoInventario>(List.of(), inv.getArgument(2), 0));
        when(repository.contarPorTipoBetween(any(), any())).thenReturn(List.of());

        service.consultarPagina(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, -5, 500);
        service.consultarPagina(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 2, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository, org.mockito.Mockito.times(2))
                .findPaginaBetween(any(), any(), captor.capture());
        assertEquals(0, captor.getAllValues().get(0).getPageNumber());
        assertEquals(100, captor.getAllValues().get(0).getPageSize());
        assertEquals(2, captor.getAllValues().get(1).getPageNumber());
        assertEquals(1, captor.getAllValues().get(1).getPageSize());
    }

    @Test
    void consultarPagina_paginaFueraDeRango_devuelveListaVaciaConTotalesCorrectos() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(9, 10);
        when(repository.findPaginaBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, pageable))
                .thenReturn(new PageImpl<>(List.<MovimientoInventario>of(), pageable, 14));
        when(repository.contarPorTipoBetween(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                total(TipoMovimiento.ENTRADA, 12), total(TipoMovimiento.AJUSTE, 2)));

        ReporteMovimientosPaginaResponse response = service.consultarPagina(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 9, 10);

        assertTrue(response.movimientos().isEmpty());
        assertEquals(14, response.totalElements());
        assertEquals(2, response.totalPages());
        assertEquals(12, response.totalEntradas());
        assertEquals(2, response.totalAjustes());
    }
}
