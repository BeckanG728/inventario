package com.taller.catalogo.producto.entity;

import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.proveedor.entity.Proveedor;
import com.taller.catalogo.unidad.entity.Unidad;
import com.taller.compartido.exception.StockInsuficienteException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(name = "costo_ultima_compra", precision = 12, scale = 2)
    private BigDecimal costoUltimaCompra;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    /** Unidad base de inventario: inmutable una vez registrado el primer movimiento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_base_id")
    private Unidad unidadBase;

    /** Presentación comercial habitual; el factor real se registra en cada movimiento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_presentacion_id")
    private Unidad unidadPresentacion;

    @Column(name = "factor_presentacion")
    private Integer factorPresentacion;

    protected Producto() {
    }

    public Producto(String codigo, String nombre, Unidad unidadBase, Integer stockMinimo,
                    Categoria categoria, Proveedor proveedor) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.unidadBase = unidadBase;
        this.stockMinimo = stockMinimo;
        this.categoria = categoria;
        this.proveedor = proveedor;
        this.stockActual = 0;
        this.activo = true;
    }

    /**
     * RN206/US-07: descuenta stock validando que el resultado nunca sea negativo.
     *
     * @param cantidadASumar cantidad a descontar, debe ser mayor que cero
     * @throws StockInsuficienteException si el stock resultante quedaría negativo
     */
    public void descontarStock(int cantidadASumar) {
        if (cantidadASumar <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor que cero");
        }

        int stockPosterior;
        try {
            stockPosterior = Math.subtractExact(this.stockActual, cantidadASumar);
        } catch (ArithmeticException ex) {
            throw new StockInsuficienteException("Stock insuficiente. Quedaría negativo.");
        }

        if (stockPosterior < 0) {
            throw new StockInsuficienteException("Stock insuficiente. Quedaría negativo.");
        }

        this.stockActual = stockPosterior;
    }
}
