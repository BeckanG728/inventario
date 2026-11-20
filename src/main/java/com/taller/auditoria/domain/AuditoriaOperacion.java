package com.taller.auditoria.domain;

import com.taller.auditoria.enums.AuditoriaTipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "auditoria_operacion")
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditoriaTipo tipo;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_username", nullable = false, length = 50)
    private String actorUsername;

    @Column(nullable = false, length = 10)
    private String metodo;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(length = 100)
    private String recurso;

    @Column(name = "recurso_id", length = 100)
    private String recursoId;

    @Column(nullable = false, length = 50)
    private String resultado;

    @Column(nullable = false)
    private boolean exito;

    public AuditoriaOperacion(Instant fecha, AuditoriaTipo tipo, Long actorId, String actorUsername,
                              String metodo, String endpoint, String recurso, String recursoId,
                              String resultado, boolean exito) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.actorId = actorId;
        this.actorUsername = actorUsername;
        this.metodo = metodo;
        this.endpoint = endpoint;
        this.recurso = recurso;
        this.recursoId = recursoId;
        this.resultado = resultado;
        this.exito = exito;
    }
}
