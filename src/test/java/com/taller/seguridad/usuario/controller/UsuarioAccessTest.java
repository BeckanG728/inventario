package com.taller.seguridad.usuario.controller;

import com.taller.configuracion.ErrorCatalogTestConfig;
import com.taller.configuracion.SecurityConfig;
import com.taller.seguridad.usuario.controller.UsuarioController;
import com.taller.seguridad.usuario.service.UsuarioDetailsService;
import com.taller.seguridad.usuario.service.UsuarioService;
import com.taller.seguridad.ratelimit.config.RateLimitProperties;
import com.taller.seguridad.ratelimit.store.InMemoryRateLimitStore;
import com.taller.seguridad.autenticacion.security.ErrorResponseWriter;
import com.taller.seguridad.autenticacion.security.JwtUtil;
import com.taller.seguridad.autenticacion.security.TokenAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
@Import({SecurityConfig.class, ErrorResponseWriter.class, TokenAuthenticationEntryPoint.class,
        RateLimitProperties.class, InMemoryRateLimitStore.class,
        ErrorCatalogTestConfig.class})
@TestPropertySource(properties = {
        "SERVER_PORT=8080",
        "JWT_SECRET=test-secret-at-least-32-characters-long!!",
        "JWT_EXPIRACION_SEGUNDOS=3600",
        "ADMIN_USERNAME=admin",
        "ADMIN_PASSWORD=Password123!",
        "ALERTAS_CRON=0 0 * * * ?",
        "RATE_LIMIT_LOGIN_IP=20",
        "RATE_LIMIT_LOGIN_USERNAME=10",
        "RATE_LIMIT_USUARIO=200",
        "RATE_LIMIT_SWAGGER=1000",
        "RATE_LIMIT_VENTANA_LOGIN_SEGUNDOS=60",
        "RATE_LIMIT_VENTANA_USUARIO_SEGUNDOS=60"
})
class UsuarioAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;
    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void mecanico_devuelve403() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "JEFE_TALLER")
    void jefeTaller_devuelve403() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_devuelve200() throws Exception {
        when(usuarioService.listar(anyInt(), anyInt(), nullable(String.class), nullable(String.class),
                nullable(Boolean.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/usuarios")).andExpect(status().isOk());
    }
}
