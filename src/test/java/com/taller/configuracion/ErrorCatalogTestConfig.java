package com.taller.configuracion;

import com.taller.auditoria.audit.AuditoriaResultadoResolver;
import com.taller.seguridad.error.AuthErrorPolicy;
import com.taller.catalogo.error.CatalogoErrorPolicy;
import com.taller.inventario.error.InventarioErrorPolicy;
import com.taller.compartido.error.ErrorPolicyResolver;
import com.taller.compartido.error.ErrorResponseFactory;
import com.taller.compartido.error.FrameworkErrorPolicy;
import com.taller.compartido.error.WebErrorPolicy;
import com.taller.catalogo.producto.error.ProductoErrorPolicy;
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
        CatalogoErrorPolicy.class,
        InventarioErrorPolicy.class,
        WebErrorPolicy.class,
        FrameworkErrorPolicy.class
})
public class ErrorCatalogTestConfig {
}
