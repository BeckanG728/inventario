package com.taller.reportes.estadisticas.service.impl;

import com.taller.compartido.ZonaHorariaNegocio;
import com.taller.inventario.api.HistorialMovimientos;
import com.taller.inventario.api.HistorialMovimientos.FechaTipo;
import com.taller.reportes.dto.response.EstadisticasTipoPunto;
import com.taller.reportes.estadisticas.enums.Granularity;
import com.taller.reportes.estadisticas.service.EstadisticasBucketing;
import com.taller.reportes.estadisticas.service.EstadisticasService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstadisticasServiceImpl implements EstadisticasService {

    private final HistorialMovimientos historialMovimientos;

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> consultar(LocalDate from, LocalDate to, Granularity granularity) {
        validar(from, to, granularity);
        Instant start = inicio(from);
        Instant endExclusive = finExclusivo(to);
        List<Instant> fechas = historialMovimientos.consultarFechasEntre(start, endExclusive);
        return EstadisticasBucketing.agrupar(fechas, granularity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadisticasTipoPunto> consultarPorTipo(LocalDate from, LocalDate to, Granularity granularity,
                                                        Long productoId) {
        validar(from, to, granularity);
        Instant start = inicio(from);
        Instant endExclusive = finExclusivo(to);
        List<FechaTipo> filas =
                historialMovimientos.consultarFechaTipoEntre(start, endExclusive, productoId);

        // Se inicializan todos los períodos para que los vacíos aparezcan en cero.
        Map<LocalDate, long[]> unidades = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal[]> costos = new LinkedHashMap<>();
        for (LocalDate periodo : EstadisticasBucketing.periodos(from, to, granularity)) {
            unidades.put(periodo, new long[3]);
            costos.put(periodo, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }
        for (FechaTipo fila : filas) {
            LocalDate bucket = EstadisticasBucketing.bucket(fila.fecha(), granularity);
            long[] contadores = unidades.get(bucket);
            BigDecimal[] importes = costos.get(bucket);
            if (contadores == null) {
                continue;
            }
            switch (fila.tipo()) {
                case ENTRADA -> {
                    contadores[0] += unidades(fila.cantidad());
                    importes[0] = importes[0].add(costo(fila.cantidad(), fila.costoUnitario()));
                }
                case CONSUMO -> {
                    contadores[1] += unidades(fila.cantidad());
                    importes[1] = importes[1].add(costo(fila.cantidad(), fila.costoUnitario()));
                }
                case AJUSTE -> contadores[2] += Math.abs(unidades(fila.variacion()));
            }
        }
        BigDecimal cero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return unidades.keySet().stream()
                .map(periodo -> new EstadisticasTipoPunto(
                        periodo,
                        unidades.get(periodo)[0], unidades.get(periodo)[1], unidades.get(periodo)[2],
                        escalar(costos.get(periodo)[0]), escalar(costos.get(periodo)[1]), cero))
                .toList();
    }

    private void validar(LocalDate from, LocalDate to, Granularity granularity) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Los parámetros from y to son obligatorios");
        }
        if (granularity == null) {
            throw new IllegalArgumentException("El parámetro granularity es obligatorio (DAY, WEEK o MONTH)");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
    }

    private Instant inicio(LocalDate desde) {
        return desde.atStartOfDay(ZonaHorariaNegocio.ZONA_NEGOCIO).toInstant();
    }

    private Instant finExclusivo(LocalDate hasta) {
        return hasta.plusDays(1).atStartOfDay(ZonaHorariaNegocio.ZONA_NEGOCIO).toInstant();
    }

    private static long unidades(Integer valor) {
        return valor == null ? 0L : valor;
    }

    private static BigDecimal costo(Integer cantidad, BigDecimal costoUnitario) {
        if (cantidad == null || costoUnitario == null) {
            return BigDecimal.ZERO;
        }
        return costoUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    private static BigDecimal escalar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
