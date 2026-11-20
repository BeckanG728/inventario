package com.taller.auditoria.config;

import com.taller.auditoria.audit.AuditoriaInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AuditoriaConfig implements WebMvcConfigurer {

    private final ApplicationEventPublisher publisher;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditoriaInterceptor(publisher))
                .addPathPatterns("/api/**");
    }
}
