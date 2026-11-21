package com.taller.seguridad.ratelimit.filter;

import com.taller.compartido.error.ErrorCode;
import com.taller.compartido.error.ErrorDefinition;
import com.taller.seguridad.autenticacion.security.ErrorResponseWriter;
import com.taller.seguridad.ratelimit.config.RateLimitProperties;
import com.taller.seguridad.ratelimit.event.SeguridadRechazoEvent;
import com.taller.seguridad.ratelimit.store.RateLimitStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

/** Limita intentos de login por IP y por username antes de JwtAuthFilter. */
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_BODY = 8192;

    private final RateLimitStore store;
    private final RateLimitProperties properties;
    private final ErrorResponseWriter errorResponseWriter;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !RateLimitPaths.LOGIN.equals(request.getRequestURI())
                || !"POST".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        byte[] cuerpo = request.getInputStream().readNBytes(MAX_BODY);
        String username = extraerUsername(cuerpo);

        String ip = RateLimitPaths.ip(request);
        Duration ventana = Duration.ofSeconds(properties.getVentanaLoginSegundos());
        RateLimitStore.Resultado porIp = store.incrementar("login:ip:" + ip, ventana);
        RateLimitStore.Resultado porUsuario = username == null
                ? null
                : store.incrementar("login:user:" + username.toLowerCase(Locale.ROOT), ventana);

        boolean excedeIp = porIp.contador() > properties.getLoginIp();
        boolean excedeUsuario = porUsuario != null && porUsuario.contador() > properties.getLoginUsername();
        if (excedeIp || excedeUsuario) {
            long reintentar = 0;
            if (excedeIp) {
                reintentar = porIp.reintentarEnSegundos();
            }
            if (excedeUsuario) {
                reintentar = Math.max(reintentar, porUsuario.reintentarEnSegundos());
            }
            String limite = excedeIp && excedeUsuario ? "LOGIN_IP_Y_USERNAME"
                    : excedeIp ? "LOGIN_IP" : "LOGIN_USERNAME";
            publisher.publishEvent(new SeguridadRechazoEvent(
                    limite, ip, username, request.getRequestURI(), reintentar));
            rechazar(request, response, reintentar);
            return;
        }

        filterChain.doFilter(new BodyCachingRequestWrapper(request, cuerpo), response);
    }

    private String extraerUsername(byte[] cuerpo) {
        if (cuerpo.length == 0) {
            return null;
        }
        try {
            JsonNode nodo = objectMapper.readTree(cuerpo);
            if (nodo == null || !nodo.isObject()) {
                return null;
            }
            String valor = nodo.path("username").asText(null);
            return valor == null || valor.isBlank() ? null : valor.trim();
        } catch (Exception ex) {
            return null;
        }
    }

    private void rechazar(HttpServletRequest request, HttpServletResponse response, long reintentar)
            throws IOException {
        response.setHeader("Retry-After", String.valueOf(reintentar));
        errorResponseWriter.write(request, response, ErrorDefinition.of(ErrorCode.RATE_LIMIT_EXCEEDED));
    }
}
