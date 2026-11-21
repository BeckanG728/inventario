package com.taller.seguridad.api;

import com.taller.auditoria.api.ProveedorActor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Principal autenticado expuesto a los controladores.
 *
 * <p>Solo contiene identificador, username y autoridades: sin credenciales y sin
 * dependencias de persistencia.</p>
 */
public record ActorAutenticado(
        Long id,
        String username,
        Collection<? extends GrantedAuthority> authorities
) implements ProveedorActor {

    public ActorAutenticado {
        authorities = List.copyOf(authorities);
    }

    @Override
    public Long actorId() {
        return id;
    }

    @Override
    public String actorUsername() {
        return username;
    }
}
