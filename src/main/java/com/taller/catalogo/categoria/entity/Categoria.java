package com.taller.catalogo.categoria.entity;

import com.taller.catalogo.unidad.entity.Unidad;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "categoria")
@Getter
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    /** Unidades válidas para los productos de esta categoría (tabla categoria_unidad). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "categoria_unidad",
            joinColumns = @JoinColumn(name = "categoria_id"),
            inverseJoinColumns = @JoinColumn(name = "unidad_id"))
    private Set<Unidad> unidades = new LinkedHashSet<>();

    protected Categoria() {
    }

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public void reemplazarUnidades(Set<Unidad> nuevas) {
        this.unidades.clear();
        if (nuevas != null) {
            this.unidades.addAll(nuevas);
        }
    }

    public boolean admiteUnidad(Unidad unidad) {
        return unidad != null && this.unidades.contains(unidad);
    }
}