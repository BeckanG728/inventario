package com.taller.seguridad.ratelimit.filter;

import com.taller.compartido.error.ErrorCode;
import com.taller.compartido.error.ErrorDefinition;
import com.taller.seguridad.autenticacion.security.ErrorResponseWriter;
import com.taller.seguridad.api.ActorAutenticado;
import com.taller.seguridad.ratelimit.config.RateLimitProperties;
import com.taller.seguridad.ratelimit.event.SeguridadRechazoEvent;
import com.taller.seguridad.ratelimit.store.RateLimitStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/** Limita el resto de peticiones autenticadas por usuario, después de JwtAuthFilter. */
@RequiredArgsConstructor
public class UsuarioRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitStore store;
    private final RateLimitProperties properties;
    private final ErrorResponseWriter errorResponseWriter;
    private final ApplicationEventPublisher publisher;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return RateLimitPaths.esExcluido(uri) || RateLimitPaths.LOGIN.equals(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ActorAutenticado actor = actorAutenticado();
        if (actor == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        boolean swagger = RateLimitPaths.esSwagger(uri);
        int limite = swagger ? properties.getSwagger() : properties.getUsuario();
        Duration ventana = Duration.ofSeconds(properties.getVentanaUsuarioSegundos());
        RateLimitStore.Resultado resultado = store.incrementar("usuario:" + actor.id(), ventana);

        if (resultado.contador() > limite) {
            publisher.publishEvent(new SeguridadRechazoEvent(
                    swagger ? "USUARIO_SWAGGER" : "USUARIO",
                    RateLimitPaths.ip(request),
                    actor.username(),
                    uri,
                    resultado.reintentarEnSegundos()));
            response.setHeader("Retry-After", String.valueOf(resultado.reintentarEnSegundos()));
            errorResponseWriter.write(request, response, ErrorDefinition.of(ErrorCode.RATE_LIMIT_EXCEEDED));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private ActorAutenticado actorAutenticado() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || !(autenticacion.getPrincipal() instanceof ActorAutenticado actor)) {
            return null;
        }
        return actor;
    }
}
