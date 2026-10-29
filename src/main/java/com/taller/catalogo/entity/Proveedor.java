package com.taller.catalogo.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "proveedor")
@Getter
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ruc;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(length = 100)
    private String contacto;

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Column(length = 255)
    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;

    protected Proveedor() {
    }

    public Proveedor(String ruc, String razonSocial, String contacto, String telefono,
                     String correo, String direccion) {
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.contacto = contacto;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.activo = true;
    }
}