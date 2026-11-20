package com.taller.config;

import com.taller.auditoria.audit.AuditoriaResultadoResolver;
import com.taller.auth.error.AuthErrorPolicy;
import com.taller.common.error.DominioErrorPolicy;
import com.taller.common.error.ErrorPolicyResolver;
import com.taller.common.error.ErrorResponseFactory;
import com.taller.common.error.GenericErrorPolicy;
import com.taller.common.error.WebErrorPolicy;
import com.taller.producto.error.ProductoErrorPolicy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Expone el catálogo de errores en tests WebMvc sin arrancar el contexto completo. */
@Configuration
@Import({
        ClockConfig.class,
        AuditoriaResultadoResolver.class,
        ErrorPolicyResolver.class,
        ErrorResponseFactory.class,
        AuthErrorPolicy.class,
        ProductoErrorPolicy.class,
        DominioErrorPolicy.class,
        WebErrorPolicy.class,
        GenericErrorPolicy.class
})
public class ErrorCatalogTestConfig {
}
