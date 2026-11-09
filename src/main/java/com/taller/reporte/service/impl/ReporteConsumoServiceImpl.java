package com.taller.reporte.service.impl;

import com.taller.config.ClockConfig;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.producto.entity.Producto;
import com.taller.reporte.dto.response.ReporteConsumoItem;
import com.taller.reporte.repository.MovimientoReporteRepository;
import com.taller.reporte.service.ReporteConsumoService;
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

    private final MovimientoReporteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ReporteConsumoItem> consultar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parámetros desde y hasta son obligatorios");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }

        ZoneId zone = ClockConfig.ZONA_NEGOCIO;
        Instant start = desde.atStartOfDay(zone).toInstant();
        Instant endExclusive = hasta.plusDays(1).atStartOfDay(zone).toInstant();

        List<MovimientoInventario> consumos = repository.findMovimientosConDetalleBetweenAndTipo(
                start, endExclusive, TipoMovimiento.CONSUMO);

        Map<ClaveConsumo, Acumulado> agrupados = new LinkedHashMap<>();
        for (MovimientoInventario movimiento : consumos) {
            ClaveConsumo clave = new ClaveConsumo(
                    movimiento.getProducto().getId(),
                    movimiento.getUsuario().getId());
            agrupados.computeIfAbsent(clave, k -> new Acumulado(
                    movimiento.getProducto(),
                    movimiento.getUsuario().getUsername()))
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

        private final Producto producto;
        private final String mecanico;
        private final Set<String> finalidades = new LinkedHashSet<>();
        private final List<Instant> fechas = new ArrayList<>();
        private long cantidadTotal;

        private Acumulado(Producto producto, String mecanico) {
            this.producto = producto;
            this.mecanico = mecanico;
        }

        private void agregar(MovimientoInventario movimiento) {
            cantidadTotal += movimiento.getCantidad() == null ? 0 : movimiento.getCantidad();

            if (movimiento.getMotivo() != null && !movimiento.getMotivo().isBlank()) {
                finalidades.add(movimiento.getMotivo());
            }
            if (movimiento.getFecha() != null) {
                fechas.add(movimiento.getFecha());
            }
        }

        private ReporteConsumoItem toResponse() {
            return new ReporteConsumoItem(
                    producto.getId(),
                    producto.getCodigo(),
                    producto.getNombre(),
                    mecanico,
                    List.copyOf(finalidades),
                    cantidadTotal,
                    List.copyOf(fechas));
        }
    }
}
