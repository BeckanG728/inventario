package com.taller.alerta.scheduler;

import com.taller.alerta.dto.response.AlertaStock;
import com.taller.alerta.service.AlertaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertaScheduler {

    private final AlertaService alertaService;

    @Scheduled(cron = "${alertas.cron}")
    public void revisarStock() {
        List<AlertaStock> alertas = alertaService.generarAlertas();
        log.info(alertaService.resumir(alertas));
        alertas.forEach(a -> log.warn("Bajo stock: {} - {} (actual {}, mínimo {}, sugerido {}, proveedor {})",
                a.codigo(), a.nombre(), a.stockActual(), a.stockMinimo(), a.cantidadSugerida(), a.proveedor()));
    }
}
