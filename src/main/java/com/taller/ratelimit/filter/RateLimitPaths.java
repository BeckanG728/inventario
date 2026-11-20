package com.taller.ratelimit.filter;

import jakarta.servlet.http.HttpServletRequest;

final class RateLimitPaths {

    static final String LOGIN = "/api/auth/login";

    private RateLimitPaths() {
    }

    static boolean esExcluido(String uri) {
        return "/actuator/health".equals(uri) || "/error".equals(uri);
    }

    static boolean esSwagger(String uri) {
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars/swagger-ui");
    }

    static String ip(HttpServletRequest request) {
        String candidato = primeraNoVacia(request.getHeader("X-Forwarded-For"));
        if (candidato != null) {
            return candidato;
        }
        candidato = primeraNoVacia(request.getHeader("X-Real-IP"));
        return candidato != null ? candidato : request.getRemoteAddr();
    }

    private static String primeraNoVacia(String cabecera) {
        if (cabecera == null || cabecera.isBlank()) {
            return null;
        }
        return cabecera.split(",", 2)[0].trim();
    }
}
