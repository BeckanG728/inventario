package com.taller.config;

import com.taller.auth.security.ErrorResponseWriter;
import com.taller.auth.security.JwtAuthFilter;
import com.taller.auth.security.JwtUtil;
import com.taller.auth.security.TokenAuthenticationEntryPoint;
import com.taller.auth.service.UsuarioDetailsService;
import com.taller.common.error.ErrorCode;
import com.taller.common.error.ErrorDefinition;
import com.taller.ratelimit.config.RateLimitProperties;
import com.taller.ratelimit.filter.LoginRateLimitFilter;
import com.taller.ratelimit.filter.UsuarioRateLimitFilter;
import com.taller.ratelimit.store.RateLimitStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UsuarioDetailsService usuarioDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter(RateLimitStore store,
                                                    RateLimitProperties properties,
                                                    ErrorResponseWriter errorResponseWriter,
                                                    ApplicationEventPublisher publisher,
                                                    ObjectMapper objectMapper) {
        return new LoginRateLimitFilter(store, properties, errorResponseWriter, publisher, objectMapper);
    }

    @Bean
    public UsuarioRateLimitFilter usuarioRateLimitFilter(RateLimitStore store,
                                                        RateLimitProperties properties,
                                                        ErrorResponseWriter errorResponseWriter,
                                                        ApplicationEventPublisher publisher) {
        return new UsuarioRateLimitFilter(store, properties, errorResponseWriter, publisher);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   LoginRateLimitFilter loginRateLimitFilter,
                                                   UsuarioRateLimitFilter usuarioRateLimitFilter,
                                                   JwtUtil jwtUtil,
                                                   UsuarioDetailsService usuarioDetailsService,
                                                   TokenAuthenticationEntryPoint authenticationEntryPoint,
                                                   ErrorResponseWriter errorResponseWriter,
                                                   AuthenticationProvider authenticationProvider) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtil, usuarioDetailsService);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                errorResponseWriter.write(request, response,
                                        ErrorDefinition.of(ErrorCode.FORBIDDEN))))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/actuator/health", "/actuator/info", "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/usuarios", "/api/usuarios/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/auditoria", "/api/auditoria/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categorias", "/api/proveedores")
                        .hasRole("JEFE_TALLER")
                        .requestMatchers(HttpMethod.POST, "/api/productos")
                        .hasRole("JEFE_TALLER")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                        .hasRole("JEFE_TALLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**")
                        .hasRole("JEFE_TALLER")
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/existencias", "/api/alertas")
                        .hasAnyRole("JEFE_TALLER", "MECANICO")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard", "/api/reportes/**", "/api/estadisticas")
                        .hasRole("JEFE_TALLER")
                        .anyRequest().authenticated())
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(usuarioRateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
