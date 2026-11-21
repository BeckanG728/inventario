package com.taller.reportes.consulta.service.impl;

import com.taller.compartido.ZonaHorariaNegocio;

import com.taller.inventario.api.HistorialMovimientos;
import com.taller.inventario.api.MovimientoDetalle;
import com.taller.reportes.dto.response.ReporteConsumoItem;
import com.taller.reportes.consulta.service.ReporteConsumoService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteConsumoServiceImpl implements ReporteConsumoService {

    private final HistorialMovimientos historialMovimientos;

    @Override
    @Transactional(readOnly = true)
    public List<ReporteConsumoItem> consultar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parámetros desde y hasta son obligatorios");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }

        ZoneId zone = ZonaHorariaNegocio.ZONA_NEGOCIO;
        Instant start = desde.atStartOfDay(zone).toInstant();
        Instant endExclusive = hasta.plusDays(1).atStartOfDay(zone).toInstant();

        List<MovimientoDetalle> consumos = historialMovimientos.consultarConsumosEntre(start, endExclusive);

        Map<ClaveConsumo, Acumulado> agrupados = new LinkedHashMap<>();
        for (MovimientoDetalle movimiento : consumos) {
            ClaveConsumo clave = new ClaveConsumo(movimiento.productoId(), movimiento.usuarioId());
            agrupados.computeIfAbsent(clave, k -> new Acumulado(
                    movimiento.productoId(),
                    movimiento.productoCodigo(),
                    movimiento.productoNombre(),
                    movimiento.usuarioUsername()))
                    .agregar(movimiento);
        }

        return agrupados.values().stream()
                .map(Acumulado::toResponse)
                .sorted(Comparator.comparing(ReporteConsumoItem::productoNombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ReporteConsumoItem::mecanico, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private record ClaveConsumo(Long productoId, Long usuarioId) {
    }

    private static final class Acumulado {

        private final Long productoId;
        private final String productoCodigo;
        private final String productoNombre;
        private final String mecanico;
        private final Set<String> finalidades = new LinkedHashSet<>();
        private final List<Instant> fechas = new ArrayList<>();
        private long cantidadTotal;

        private Acumulado(Long productoId, String productoCodigo, String productoNombre, String mecanico) {
            this.productoId = productoId;
            this.productoCodigo = productoCodigo;
            this.productoNombre = productoNombre;
            this.mecanico = mecanico;
        }

        private void agregar(MovimientoDetalle movimiento) {
            cantidadTotal += movimiento.cantidad() == null ? 0 : movimiento.cantidad();

            if (movimiento.motivo() != null && !movimiento.motivo().isBlank()) {
                finalidades.add(movimiento.motivo());
            }
            if (movimiento.fecha() != null) {
                fechas.add(movimiento.fecha());
            }
        }

        private ReporteConsumoItem toResponse() {
            return new ReporteConsumoItem(
                    productoId,
                    productoCodigo,
                    productoNombre,
                    mecanico,
                    List.copyOf(finalidades),
                    cantidadTotal,
                    List.copyOf(fechas));
        }
    }
}
