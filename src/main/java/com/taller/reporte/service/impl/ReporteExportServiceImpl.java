package com.taller.reporte.service.impl;

import com.taller.movimiento.dto.response.MovimientoResponse;
import com.taller.reporte.dto.response.ProductoBajoStock;
import com.taller.reporte.dto.response.ProveedorBajoStock;
import com.taller.reporte.dto.response.ReporteConsumoItem;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.enums.FormatoReporte;
import com.taller.reporte.export.ReporteExcelExporter;
import com.taller.reporte.export.ReportePdfExporter;
import com.taller.reporte.export.ReporteTabla;
import com.taller.reporte.service.ReporteExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteExportServiceImpl implements ReporteExportService {

    private static final List<String> COLUMNAS_MOVIMIENTOS = List.of(
            "ID", "Fecha", "Tipo", "Producto ID", "Cantidad", "Variación",
            "Stock anterior", "Stock posterior", "Costo unitario", "Motivo", "Usuario ID");
    private static final List<String> COLUMNAS_CONSUMO = List.of(
            "Producto ID", "Código", "Producto", "Mecánico", "Finalidad", "Cantidad total", "Fechas");
    private static final List<String> COLUMNAS_BAJO_STOCK = List.of(
            "Proveedor", "Código", "Producto", "Stock actual", "Stock mínimo", "Cantidad sugerida");

    private final ReportePdfExporter pdfExporter;
    private final ReporteExcelExporter excelExporter;

    @Override
    public byte[] exportarMovimientos(ReporteMovimientosResponse reporte, FormatoReporte formato) {
        String subtitulo = "Entradas: " + reporte.totalEntradas()
                + " | Consumos: " + reporte.totalConsumos()
                + " | Ajustes: " + reporte.totalAjustes();

        List<List<String>> filas = reporte.movimientos().stream()
                .map(this::filaMovimiento)
                .toList();

        return exportar(new ReporteTabla(
                "Reporte de movimientos", subtitulo, COLUMNAS_MOVIMIENTOS, filas), formato);
    }

    @Override
    public byte[] exportarConsumo(List<ReporteConsumoItem> reporte, FormatoReporte formato) {
        List<List<String>> filas = reporte.stream()
                .map(this::filaConsumo)
                .toList();

        return exportar(new ReporteTabla(
                "Reporte de consumo", "Consumos agrupados por producto y mecánico",
                COLUMNAS_CONSUMO, filas), formato);
    }

    @Override
    public byte[] exportarBajoStock(List<ProveedorBajoStock> reporte, FormatoReporte formato) {
        List<List<String>> filas = new ArrayList<>();
        for (ProveedorBajoStock proveedor : reporte) {
            for (ProductoBajoStock producto : proveedor.productos()) {
                filas.add(List.of(
                        valor(proveedor.proveedor()),
                        valor(producto.codigo()),
                        valor(producto.nombre()),
                        valor(producto.stockActual()),
                        valor(producto.stockMinimo()),
                        valor(producto.sugerido())));
            }
        }

        return exportar(new ReporteTabla(
                "Reporte de bajo stock", "Productos en alerta agrupados por proveedor",
                COLUMNAS_BAJO_STOCK, filas), formato);
    }

    private byte[] exportar(ReporteTabla tabla, FormatoReporte formato) {
        return switch (formato) {
            case PDF -> pdfExporter.generar(tabla);
            case EXCEL -> excelExporter.generar(tabla);
        };
    }

    private List<String> filaMovimiento(MovimientoResponse movimiento) {
        return List.of(
                valor(movimiento.id()),
                valor(movimiento.fecha()),
                valor(movimiento.tipo()),
                valor(movimiento.productoId()),
                valor(movimiento.cantidad()),
                valor(movimiento.variacion()),
                valor(movimiento.stockAnterior()),
                valor(movimiento.stockPosterior()),
                valor(movimiento.costoUnitario()),
                valor(movimiento.motivo()),
                valor(movimiento.usuarioId()));
    }

    private List<String> filaConsumo(ReporteConsumoItem consumo) {
        return List.of(
                valor(consumo.productoId()),
                valor(consumo.productoCodigo()),
                valor(consumo.productoNombre()),
                valor(consumo.mecanico()),
                consumo.finalidades() == null ? "" : String.join(", ", consumo.finalidades()),
                valor(consumo.cantidadTotal()),
                consumo.fechas() == null ? "" : consumo.fechas().stream()
                        .map(this::valor)
                        .collect(Collectors.joining(", ")));
    }

    private String valor(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Instant instante) {
            return instante.toString();
        }
        if (valor instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        return valor.toString();
    }
}
