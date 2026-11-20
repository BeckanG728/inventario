package com.taller.auditoria.audit;

import com.taller.auth.entity.Usuario;
import com.taller.auditoria.enums.AuditoriaTipo;
import com.taller.auditoria.event.OperacionAuditadaEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AuditoriaInterceptor implements HandlerInterceptor {

    static final String ATTR_ACTOR_ID = "auditoria.actorId";
    static final String ATTR_ACTOR_USERNAME = "auditoria.actorUsername";
    static final String ATTR_RECURSO = "auditoria.recurso";
    static final String ATTR_RECURSO_ID = "auditoria.recursoId";
    static final String ATTR_TIPO = "auditoria.tipo";
    static final String ATTR_ENDPOINT = "auditoria.endpoint";

    private final ApplicationEventPublisher publisher;
    private final AuditoriaResultadoResolver resultadoResolver;

    public AuditoriaInterceptor(ApplicationEventPublisher publisher,
                                AuditoriaResultadoResolver resultadoResolver) {
        this.publisher = publisher;
        this.resultadoResolver = resultadoResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!debeAuditar(request)) {
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long actorId = null;
        String actorUsername = "sistema";
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            actorId = usuario.getId();
            actorUsername = usuario.getUsername();
        } else if (auth != null && auth.isAuthenticated() && auth.getName() != null
                && !"anonymousUser".equals(auth.getName())) {
            actorUsername = auth.getName();
        }

        String uri = request.getRequestURI();
        String[] recurso = resolverRecurso(uri);
        request.setAttribute(ATTR_ACTOR_ID, actorId);
        request.setAttribute(ATTR_ACTOR_USERNAME, actorUsername);
        request.setAttribute(ATTR_RECURSO, recurso[0]);
        request.setAttribute(ATTR_RECURSO_ID, recurso[1]);
        request.setAttribute(ATTR_TIPO,
                uri.startsWith("/api/auth/") ? AuditoriaTipo.AUTENTICACION : AuditoriaTipo.OPERACION);
        request.setAttribute(ATTR_ENDPOINT, uri);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (request.getAttribute(ATTR_ENDPOINT) == null) {
            return;
        }
        boolean exito = ex == null && response.getStatus() < 400;
        String resultado = exito ? "OK" : resultadoResolver.resolver(ex, response.getStatus());
        try {
            publisher.publishEvent(new OperacionAuditadaEvent(
                    (AuditoriaTipo) request.getAttribute(ATTR_TIPO),
                    (Long) request.getAttribute(ATTR_ACTOR_ID),
                    (String) request.getAttribute(ATTR_ACTOR_USERNAME),
                    request.getMethod(),
                    (String) request.getAttribute(ATTR_ENDPOINT),
                    (String) request.getAttribute(ATTR_RECURSO),
                    (String) request.getAttribute(ATTR_RECURSO_ID),
                    resultado,
                    exito));
        } catch (Exception publicacionFallida) {
            log.warn("No se pudo publicar el evento de auditoría de {} {}: {}",
                    request.getMethod(), request.getRequestURI(), publicacionFallida.getMessage());
        }
    }

    private boolean debeAuditar(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String metodo = request.getMethod();
        if (!uri.startsWith("/api/") || uri.startsWith("/api/auditoria")) {
            return false;
        }
        return !"GET".equals(metodo) && !"HEAD".equals(metodo) && !"OPTIONS".equals(metodo);
    }

    private String[] resolverRecurso(String uri) {
        String[] segmentos = uri.split("/");
        for (int i = segmentos.length - 1; i >= 0; i--) {
            if (segmentos[i].matches("\\d+")) {
                String nombre = i > 0 ? segmentos[i - 1] : "recurso";
                return new String[]{singular(nombre), segmentos[i]};
            }
        }
        for (int i = segmentos.length - 1; i >= 0; i--) {
            if (!segmentos[i].isEmpty()) {
                return new String[]{singular(segmentos[i]), null};
            }
        }
        return new String[]{"desconocido", null};
    }

    private String singular(String segmento) {
        if (segmento.endsWith("s") && segmento.length() > 1) {
            return segmento.substring(0, segmento.length() - 1);
        }
        return segmento;
    }
}
