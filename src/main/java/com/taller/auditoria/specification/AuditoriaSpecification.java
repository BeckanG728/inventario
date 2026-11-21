package com.taller.auditoria.specification;

import com.taller.compartido.ZonaHorariaNegocio;

import com.taller.auditoria.domain.AuditoriaOperacion;
import com.taller.auditoria.enums.AuditoriaTipo;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

/** Filtros de consulta de la auditoría. Las fechas se interpretan en America/Lima. */
public final class AuditoriaSpecification {

    private AuditoriaSpecification() {
    }

    public static Specification<AuditoriaOperacion> filtros(LocalDate desde, LocalDate hasta,
                                                            String actor, String tipo, String resultado) {
        ZoneId zona = ZonaHorariaNegocio.ZONA_NEGOCIO;
        Specification<AuditoriaOperacion> spec = Specification.unrestricted();

        if (desde != null) {
            var inicio = desde.atStartOfDay(zona).toInstant();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), inicio));
        }
        if (hasta != null) {
            var finExclusivo = hasta.plusDays(1).atStartOfDay(zona).toInstant();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("fecha"), finExclusivo));
        }
        if (actor != null && !actor.isBlank()) {
            String patron = "%" + actor.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("actorUsername")), patron));
        }
        if (tipo != null && !tipo.isBlank()) {
            AuditoriaTipo filtroTipo = parsearTipo(tipo);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), filtroTipo));
        }
        if (resultado != null && !resultado.isBlank()) {
            spec = spec.and(resolverResultado(resultado.trim()));
        }
        return spec;
    }

    private static AuditoriaTipo parsearTipo(String tipo) {
        try {
            return AuditoriaTipo.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de operación inválido: " + tipo);
        }
    }

    private static Specification<AuditoriaOperacion> resolverResultado(String resultado) {
        if (resultado.equalsIgnoreCase("OK")) {
            return (root, query, cb) -> cb.isTrue(root.get("exito"));
        }
        if (resultado.equalsIgnoreCase("ERROR")) {
            return (root, query, cb) -> cb.isFalse(root.get("exito"));
        }
        String filtro = resultado.toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("resultado")), filtro);
    }
}
