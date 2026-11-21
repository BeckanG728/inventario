package com.taller.reportes.consulta.service.impl;

import com.taller.inventario.api.HistorialMovimientos;
import com.taller.inventario.api.MovimientoDetalle;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.reportes.dto.response.ReporteConsumoItem;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * US-18 — Reporte de consumo por periodo (RN404).
 */
@ExtendWith(MockitoExtension.class)
class ReporteConsumoServiceImplTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Instant INICIO_ENERO = LocalDate.of(2026, 1, 1).atStartOfDay(ZONE).toInstant();
    private static final Instant FIN_ENERO_EXCLUSIVO = LocalDate.of(2026, 2, 1).atStartOfDay(ZONE).toInstant();

    @Mock
    private HistorialMovimientos historialMovimientos;

    @InjectMocks
    private ReporteConsumoServiceImpl service;

    private MovimientoDetalle consumo(Long productoId, String codigo, String nombre,
                                      Long usuarioId, String username,
                                      int cantidad, String finalidad, Instant fecha) {
        return new MovimientoDetalle(
                1L, fecha, TipoMovimiento.CONSUMO, cantidad, null, 10, 10 - cantidad,
                null, finalidad, productoId, codigo, nombre, null, usuarioId, username);
    }

    @Test
    void consultar_sinDesde_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.consultar(null, LocalDate.of(2026, 1, 31)));
        verifyNoInteractions(historialMovimientos);
    }

    @Test
    void consultar_sinHasta_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.consultar(LocalDate.of(2026, 1, 1), null));
        verifyNoInteractions(historialMovimientos);
    }

    @Test
    void consultar_hastaAnteriorADesde_lanzaErrorConMensajeEsperado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.consultar(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31)));

        assertEquals("La fecha final no puede ser anterior a la inicial", ex.getMessage());
        verifyNoInteractions(historialMovimientos);
    }

    @Test
    void consultar_consultaElRangoCompletoConLimiteSuperiorExclusivo() {
        when(historialMovimientos.consultarConsumosEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO))
                .thenReturn(List.of());

        service.consultar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        verify(historialMovimientos).consultarConsumosEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO);
    }

    @Test
    void consultar_sinConsumos_devuelveListaVacia() {
        when(historialMovimientos.consultarConsumosEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO))
                .thenReturn(List.of());

        List<ReporteConsumoItem> reporte = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertTrue(reporte.isEmpty());
    }

    @Test
    void consultar_agrupaPorProductoYMecanicoYSumaCantidades() {
        Instant primeraFecha = LocalDate.of(2026, 1, 10).atStartOfDay(ZONE).toInstant();
        Instant segundaFecha = LocalDate.of(2026, 1, 15).atStartOfDay(ZONE).toInstant();

        when(historialMovimientos.consultarConsumosEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                consumo(1L, "ACE-01", "Aceite 10W40", 2L, "juan", 2, "Cambio de aceite", primeraFecha),
                consumo(1L, "ACE-01", "Aceite 10W40", 2L, "juan", 3, "Cambio de aceite", segundaFecha)));

        List<ReporteConsumoItem> reporte = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, reporte.size());
        ReporteConsumoItem item = reporte.get(0);
        assertEquals(1L, item.productoId());
        assertEquals("ACE-01", item.productoCodigo());
        assertEquals("Aceite 10W40", item.productoNombre());
        assertEquals("juan", item.mecanico());
        assertEquals(5, item.cantidadTotal());
        assertEquals(List.of("Cambio de aceite"), item.finalidades());
        assertEquals(List.of(primeraFecha, segundaFecha), item.fechas());
    }

    @Test
    void consultar_mismoProductoConMecanicosDistintos_generaUnaFilaPorMecanico() {
        Instant fecha = LocalDate.of(2026, 1, 10).atStartOfDay(ZONE).toInstant();

        when(historialMovimientos.consultarConsumosEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                consumo(1L, "FILT-01", "Filtro Aceite", 2L, "juan", 1, "Mantenimiento", fecha),
                consumo(1L, "FILT-01", "Filtro Aceite", 3L, "pedro", 4, "Reparación", fecha)));

        List<ReporteConsumoItem> reporte = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(2, reporte.size());
        assertEquals("juan", reporte.get(0).mecanico());
        assertEquals(1, reporte.get(0).cantidadTotal());
        assertEquals("pedro", reporte.get(1).mecanico());
        assertEquals(4, reporte.get(1).cantidadTotal());
    }

    @Test
    void consultar_conservaLaFinalidadYLasFechasDeclaradas() {
        Instant fecha = LocalDate.of(2026, 1, 10).atStartOfDay(ZONE).toInstant();

        when(historialMovimientos.consultarConsumosEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                consumo(1L, "FILT-01", "Filtro Aceite", 2L, "juan", 1, "Cambio de aceite", fecha),
                consumo(1L, "FILT-01", "Filtro Aceite", 2L, "juan", 1, "Revisión", fecha)));

        ReporteConsumoItem item = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)).get(0);

        assertEquals(List.of("Cambio de aceite", "Revisión"), item.finalidades());
        assertEquals(2, item.fechas().size());
        assertEquals(2, item.cantidadTotal());
    }

    @Test
    void consultar_incluyeMovimientosHistoricosDeProductosInactivos() {
        Instant fecha = LocalDate.of(2026, 1, 10).atStartOfDay(ZONE).toInstant();

        when(historialMovimientos.consultarConsumosEntre(INICIO_ENERO, FIN_ENERO_EXCLUSIVO)).thenReturn(List.of(
                consumo(1L, "VIE-01", "Producto histórico", 2L, "juan", 2, "Uso histórico", fecha)));

        List<ReporteConsumoItem> reporte = service.consultar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, reporte.size());
        assertEquals("Producto histórico", reporte.get(0).productoNombre());
    }
}
