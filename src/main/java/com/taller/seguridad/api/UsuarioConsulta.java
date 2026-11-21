package com.taller.seguridad.api;

import java.util.List;
import java.util.Map;

/** Consulta pública de datos no sensibles de usuarios, para otros dominios. */
public interface UsuarioConsulta {

    /** Usernames por identificador. No expone credenciales ni entidades. */
    Map<Long, String> consultarUsuariosPorIds(List<Long> ids);
}
