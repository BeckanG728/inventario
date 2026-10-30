package com.taller.reporte.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.producto.entity.Producto;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.repository.MovimientoReporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
