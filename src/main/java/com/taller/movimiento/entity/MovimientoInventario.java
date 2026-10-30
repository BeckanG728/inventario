package com.taller.movimiento.entity;

import com.taller.auth.entity.Usuario;
import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.producto.entity.Producto;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "consumo_id")
    private Long consumoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    protected MovimientoInventario() {
    }

    public MovimientoInventario(TipoMovimiento tipo, Producto producto, Usuario usuario,
                                Integer stockAnterior, Integer stockPosterior, Integer variacion,
                                String motivo) {
        this.tipo = tipo;
        this.producto = producto;
        this.usuario = usuario;
        this.stockAnterior = stockAnterior;
        this.stockPosterior = stockPosterior;
        this.variacion = variacion;
        this.motivo = motivo;
    }
}
