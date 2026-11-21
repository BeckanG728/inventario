package com.taller.inventario.movimiento.entity;

import com.taller.inventario.movimiento.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "movimiento_inventario")
@Getter
@Setter
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    private Integer cantidad;

    private Integer variacion;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_posterior", nullable = false)
    private Integer stockPosterior;

    @Column(name = "costo_unitario", precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(length = 255)
    private String motivo;

    /** Identificador del producto de catálogo; la FK se conserva en la base de datos. */
    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "consumo_id")
    private Long consumoId;

    /** Identificador del usuario de seguridad; la FK se conserva en la base de datos. */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    protected MovimientoInventario() {
    }

    public MovimientoInventario(TipoMovimiento tipo, Long productoId, Long usuarioId,
                                Integer stockAnterior, Integer stockPosterior, Integer variacion,
                                String motivo) {
        this.tipo = tipo;
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.stockAnterior = stockAnterior;
        this.stockPosterior = stockPosterior;
        this.variacion = variacion;
        this.motivo = motivo;
    }
}
