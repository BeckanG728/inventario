package com.taller.inventario.movimiento.service.impl;

import com.taller.inventario.api.HistorialMovimientos;
import com.taller.inventario.api.MovimientoDetalle;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.dto.response.ReporteMovimientosResponse;
import com.taller.inventario.movimiento.dto.response.ReporteMovimientosPaginaResponse;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

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
    private HistorialMovimientos historialMovimientos;

    @InjectMocks
    private ReporteMovimientosServiceImpl service;

    /** El estado activo del producto no afecta al movimiento histórico (solo se conserva su identificador). */
    private MovimientoDetalle movimiento(long id, TipoMovimiento tipo, boolean productoActivo) {
        return new MovimientoDetalle(
                id, Instant.parse("2026-01-10T00:00:00Z"), tipo, 2, -5, 10, 5,
                null, "motivo", 1L, "P-1", "Filtro de aceite", null, 2L, "mecanico",
                null, null, null);
    }

    // ---------- RN405: validación del rango de fechas ----------

    @Test
    void consultar_sinDesde_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consultar(null, LocalDate.of(2026, 1, 31), null));
        verifyNoInteractions(historialMovimientos);
    }

    @Test
    void consultar_sinHasta_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consultar(LocalDate.of(2026, 1, 1), null, null));
        verifyNoInteractions(historialMovimientos);
    }

    @Test
    void consultar_hastaAnteriorADesde_lanzaErrorConMensajeEsperado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.consultar(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31), null));
        assertEquals("La fecha final no puede ser anterior a la inicial", ex.getMessage());
        verifyNoInteractions(historialMovimientos);
    }

    @Test
    void consultar_desdeIgualAHasta_esValidoEIncluyeElDiaCompleto() {
        LocalDate dia = LocalDate.of(2026, 1, 15);
        when(historialMovimientos.consultarEntre(
                LocalDate.of(2026, 1, 15).atStartOfDay(ZONE).toInstant(),
                LocalDate.of(2026, 1, 16).atStartOfDay(ZONE).toInstant(), null))
                .thenReturn(List.of());

        ReporteMovimientosResponse response = service.consultar(dia, dia, null);

        assertTrue(response.movimientos().isEmpty());
    }

    @Test
    void consultar_hastaEsInclusivo_usaLimiteSuperiorExclusivoAlDiaSiguiente() {
        when(historialMovimientos.consultarEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null))
                .thenReturn(List.of());

        service.consultar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        verify(historialMovimientos).consultarEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null);
    }

    // ---------- Filtro por tipo y totales ----------

    @Test
    void consultar_sinTipo_noUsaLaConsultaFiltrada() {
        when(historialMovimientos.consultarEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null))
                .thenReturn(List.of());

        service.consultar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        verify(historialMovimientos, never())
                .consultarEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, TipoMovimiento.ENTRADA);
    }

    @Test
    void consultar_conTipo_usaLaConsultaFiltradaPorTipo() {
        when(historialMovimientos.consultarEntre(
                INICIO_ENERO, FIN_ENERO_EXCLUSIVO, TipoMovimiento.CONSUMO))
                .thenReturn(List.of(movimiento(1, TipoMovimiento.CONSUMO, true)));

        ReporteMovimientosResponse response = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), TipoMovimiento.CONSUMO);

        assertEquals(0, response.totalEntradas());
        assertEquals(1, response.totalConsumos());
        assertEquals(0, response.totalAjustes());
        assertEquals(1, response.movimientos().size());
        verify(historialMovimientos, never()).consultarEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null);
    }

    @Test
    void consultar_calculaTotalesPorTipo() {
        when(historialMovimientos.consultarEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null)).thenReturn(List.of(
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
        when(historialMovimientos.consultarEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null)).thenReturn(List.of(
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
        when(historialMovimientos.consultarEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null))
                .thenReturn(List.of());

        ReporteMovimientosResponse response = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null);

        assertEquals(0, response.totalEntradas());
        assertEquals(0, response.totalConsumos());
        assertEquals(0, response.totalAjustes());
        assertTrue(response.movimientos().isEmpty());
    }

    // ---------- Paginación (CATALOGO_ENDPOINTS §9.1, M400-07) ----------

    @Test
    void consultarPagina_sinDesde_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consultarPagina(null, LocalDate.of(2026, 1, 31), null, 0, 10));
        verifyNoInteractions(historialMovimientos);
    }

    @Test
    void consultarPagina_sinTipo_devuelvePaginaYTotalesDeTodoElFiltro() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 2);
        when(historialMovimientos.consultarPagina(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null, pageable))
                .thenReturn(new PageImpl<>(
                        List.of(movimiento(1, TipoMovimiento.ENTRADA, true),
                                movimiento(2, TipoMovimiento.CONSUMO, true)),
                        pageable, 6));
        when(historialMovimientos.contarPorTipoEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO))
                .thenReturn(Map.of(
                        TipoMovimiento.ENTRADA, 2L,
                        TipoMovimiento.CONSUMO, 1L,
                        TipoMovimiento.AJUSTE, 3L));

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
        when(historialMovimientos.consultarPagina(
                INICIO_ENERO, FIN_ENERO_EXCLUSIVO, TipoMovimiento.ENTRADA, pageable))
                .thenReturn(new PageImpl<>(List.of(
                        movimiento(1, TipoMovimiento.ENTRADA, true),
                        movimiento(2, TipoMovimiento.ENTRADA, true)), pageable, 2));
        when(historialMovimientos.contarPorTipoEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO))
                .thenReturn(Map.of(TipoMovimiento.ENTRADA, 2L, TipoMovimiento.CONSUMO, 1L));

        ReporteMovimientosPaginaResponse response = service.consultarPagina(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), TipoMovimiento.ENTRADA, 0, 10);

        assertEquals(2, response.totalEntradas());
        assertEquals(0, response.totalConsumos());
        assertEquals(0, response.totalAjustes());
        assertEquals(2, response.totalElements());
        verify(historialMovimientos).consultarPagina(
                INICIO_ENERO, FIN_ENERO_EXCLUSIVO, TipoMovimiento.ENTRADA, pageable);
        verify(historialMovimientos, never()).consultarPagina(any(), any(), isNull(), any());
    }

    @Test
    void consultarPagina_normalizaPageYSize() {
        when(historialMovimientos.consultarPagina(any(), any(), any(), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<MovimientoDetalle>(List.of(), inv.getArgument(3), 0));
        when(historialMovimientos.contarPorTipoEntre(any(), any())).thenReturn(Map.of());

        service.consultarPagina(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, -5, 500);
        service.consultarPagina(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 2, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(historialMovimientos, org.mockito.Mockito.times(2))
                .consultarPagina(any(), any(), any(), captor.capture());
        assertEquals(0, captor.getAllValues().get(0).getPageNumber());
        assertEquals(100, captor.getAllValues().get(0).getPageSize());
        assertEquals(2, captor.getAllValues().get(1).getPageNumber());
        assertEquals(1, captor.getAllValues().get(1).getPageSize());
    }

    @Test
    void consultarPagina_paginaFueraDeRango_devuelveListaVaciaConTotalesCorrectos() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(9, 10);
        when(historialMovimientos.consultarPagina(INICIO_ENERO, FIN_ENERO_EXCLUSIVO, null, pageable))
                .thenReturn(new PageImpl<>(List.<MovimientoDetalle>of(), pageable, 14));
        when(historialMovimientos.contarPorTipoEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO))
                .thenReturn(Map.of(TipoMovimiento.ENTRADA, 12L, TipoMovimiento.AJUSTE, 2L));

        ReporteMovimientosPaginaResponse response = service.consultarPagina(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 9, 10);

        assertTrue(response.movimientos().isEmpty());
        assertEquals(14, response.totalElements());
        assertEquals(2, response.totalPages());
        assertEquals(12, response.totalEntradas());
        assertEquals(2, response.totalAjustes());
    }
}
