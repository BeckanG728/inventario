package com.taller.compartido.error;

import com.taller.seguridad.error.AuthErrorPolicy;
import com.taller.seguridad.exception.InvalidRoleException;
import com.taller.catalogo.error.CatalogoErrorPolicy;
import com.taller.inventario.error.InventarioErrorPolicy;
import com.taller.compartido.exception.DuplicateResourceException;
import com.taller.catalogo.producto.error.ProductoErrorPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorPolicyResolverTest {

    private final ErrorPolicyResolver resolver = new ErrorPolicyResolver(List.of(
            new AuthErrorPolicy(),
            new ProductoErrorPolicy(),
            new CatalogoErrorPolicy(),
            new InventarioErrorPolicy(),
            new WebErrorPolicy(),
            new FrameworkErrorPolicy()));

    @Test
    void excepcionConocida_devuelveDefinicionDeSuPolitica() {
        ErrorDefinition definition = resolver.resolveMvc(new InvalidRoleException("rol inválido"));

        assertEquals(HttpStatus.BAD_REQUEST, definition.status());
        assertEquals("INVALID_ROLE", definition.code());
        assertEquals("rol inválido", definition.message());
    }

    @Test
    void excepcionDeOtroModulo_devuelveDefinicionDeSuPolitica() {
        ErrorDefinition definition = resolver.resolveMvc(new DuplicateResourceException("duplicado"));

        assertEquals(HttpStatus.CONFLICT, definition.status());
        assertEquals("DUPLICATE_RESOURCE", definition.code());
    }

    @Test
    void excepcionDesconocidaEnMvc_devuelveInternalError() {
        ErrorDefinition definition = resolver.resolveMvc(new RuntimeException());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, definition.status());
        assertEquals("INTERNAL_ERROR", definition.code());
    }

    @Test
    void auditoriaConExcepcionDesconocida_devuelveHttpDelEstado() {
        assertEquals("HTTP_503", resolver.resolveAuditCode(new RuntimeException(), 503));
    }

    @Test
    void auditoriaSinExcepcionYEstadoOk_devuelveOk() {
        assertEquals("OK", resolver.resolveAuditCode(null, 200));
    }
}
