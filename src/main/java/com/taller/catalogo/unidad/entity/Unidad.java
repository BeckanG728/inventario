package com.taller.catalogo.unidad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Unidad de inventario del catálogo de negocio (sembrada en V9). */
@Entity
@Table(name = "unidad")
@Getter
@Setter
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(name = "permite_fragmentacion", nullable = false)
    private boolean permiteFragmentacion;

    @Column(nullable = false)
    private boolean activo = true;

    protected Unidad() {
    }

    public Unidad(String codigo, String nombre, boolean permiteFragmentacion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.permiteFragmentacion = permiteFragmentacion;
        this.activo = true;
    }
}
