package com.taller.auditoria.api;

/**
 * Identidad del actor que auditoría necesita capturar en el momento de la operación.
 *
 * <p>Auditoría define el contrato; seguridad lo implementa. Así no se consulta al usuario
 * con posterioridad y se conserva la identidad histórica en el asiento.</p>
 */
public interface ProveedorActor {

    Long actorId();

    String actorUsername();
}
