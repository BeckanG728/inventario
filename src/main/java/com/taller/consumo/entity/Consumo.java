package com.taller.consumo.entity;

import com.taller.auth.entity.Usuario;
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

import java.time.Instant;

@Entity
@Table(name = "consumo")
@Getter
@Setter
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false, length = 255)
    private String finalidad;

    @Column(name = "referencia_vehiculo_cliente", length = 150)
    private String referenciaVehiculoCliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    protected Consumo() {
    }

    public Consumo(Instant fecha, String finalidad, String referenciaVehiculoCliente, Usuario usuario) {
        this.fecha = fecha;
        this.finalidad = finalidad;
        this.referenciaVehiculoCliente = referenciaVehiculoCliente;
        this.usuario = usuario;
    }
}
