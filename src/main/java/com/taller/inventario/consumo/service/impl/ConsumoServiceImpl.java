package com.taller.inventario.consumo.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.inventario.consumo.domain.ConsumoItem;
import com.taller.inventario.consumo.dto.request.ConsumoRequest;
import com.taller.inventario.consumo.dto.response.ConsumoResponse;
import com.taller.inventario.consumo.entity.Consumo;
import com.taller.inventario.consumo.repository.ConsumoRepository;
import com.taller.inventario.consumo.service.ConsumoService;
import com.taller.inventario.consumo.service.ConsumoStockService;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumoServiceImpl implements ConsumoService {

    private final ConsumoRepository consumoRepository;
    private final ConsumoStockService consumoStockService;
    private final Clock clock;

    @Override
    @Transactional
    public ConsumoResponse registrar(ConsumoRequest request, Usuario usuario) {
        String finalidad = request.finalidad().trim();
        Consumo consumo = consumoRepository.save(new Consumo(
                Instant.now(clock), finalidad, normalizarReferencia(request.referenciaVehiculoCliente()), usuario));

        List<ConsumoItem> items = request.items().stream()
                .map(item -> new ConsumoItem(item.productoId(), item.cantidad()))
                .toList();

        List<MovimientoInventario> movimientos = consumoStockService.validarYDescontarStock(
                items, usuario, consumo.getId(), finalidad);

        return ConsumoResponse.from(consumo, movimientos);
    }

    private String normalizarReferencia(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return null;
        }
        return referencia.trim();
    }
}
