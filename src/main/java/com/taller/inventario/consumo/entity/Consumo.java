package com.taller.inventario.consumo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    /** Identificador del usuario de seguridad; la FK se conserva en la base de datos. */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    protected Consumo() {
    }

    public Consumo(Instant fecha, String finalidad, String referenciaVehiculoCliente, Long usuarioId) {
        this.fecha = fecha;
        this.finalidad = finalidad;
        this.referenciaVehiculoCliente = referenciaVehiculoCliente;
        this.usuarioId = usuarioId;
    }
}
