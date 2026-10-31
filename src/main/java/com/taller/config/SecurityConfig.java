package com.taller.config;

import com.taller.auth.security.ErrorResponseWriter;
import com.taller.auth.security.JwtAuthFilter;
import com.taller.auth.security.JwtUtil;
import com.taller.auth.security.TokenAuthenticationEntryPoint;
import com.taller.auth.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
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
                                errorResponseWriter.write(request, response, HttpStatus.FORBIDDEN,
                                        "FORBIDDEN", "No tiene permisos para realizar esta acción")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/actuator/health", "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register")
                        .hasRole("ADMIN")
                        .requestMatchers(
                                "/api/productos/guardar",
                                "/api/categorias/guardar",
                                "/api/proveedores/guardar")
                        .hasRole("JEFE_TALLER")
                        .requestMatchers(HttpMethod.POST, "/api/productos")
                        .hasRole("JEFE_TALLER")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                        .hasRole("JEFE_TALLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**")
                        .hasRole("JEFE_TALLER")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard")
                        .hasAnyRole("JEFE_TALLER", "MECANICO")
                        .requestMatchers(HttpMethod.POST, "/api/vehiculos")
                        .hasRole("JEFE_TALLER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
