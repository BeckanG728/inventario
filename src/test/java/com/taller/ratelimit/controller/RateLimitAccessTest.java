package com.taller.ratelimit.controller;

import com.taller.auth.controller.AuthController;
import com.taller.auth.dto.response.LoginResponse;
import com.taller.auth.security.ErrorResponseWriter;
import com.taller.auth.security.JwtUtil;
import com.taller.auth.security.TokenAuthenticationEntryPoint;
import com.taller.auth.service.AuthService;
import com.taller.auth.service.UsuarioDetailsService;
import com.taller.config.SecurityConfig;
import com.taller.ratelimit.config.RateLimitProperties;
import com.taller.ratelimit.store.InMemoryRateLimitStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, ErrorResponseWriter.class, TokenAuthenticationEntryPoint.class,
        RateLimitProperties.class, InMemoryRateLimitStore.class})
@TestPropertySource(properties = {
        "SERVER_PORT=8080",
        "JWT_SECRET=test-secret-at-least-32-characters-long!!",
        "JWT_EXPIRACION_SEGUNDOS=3600",
        "ADMIN_USERNAME=admin",
        "ADMIN_PASSWORD=Password123!",
        "ALERTAS_CRON=0 0 * * * ?",
        "RATE_LIMIT_LOGIN_IP=2",
        "RATE_LIMIT_LOGIN_USERNAME=2",
        "RATE_LIMIT_USUARIO=200",
        "RATE_LIMIT_SWAGGER=1000",
        "RATE_LIMIT_VENTANA_LOGIN_SEGUNDOS=60",
        "RATE_LIMIT_VENTANA_USUARIO_SEGUNDOS=60"
})
class RateLimitAccessTest {

    private static final String LOGIN = "{\"username\":\"juan\",\"password\":\"Password123!\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @BeforeEach
    void setUp() {
        when(authService.login(any())).thenReturn(new LoginResponse("token", "Bearer", 3600, 1L, "MECANICO"));
    }

    @Test
    void superarElLimiteDeLogin_devuelve429ConElContratoDeErrores() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", matchesPattern("\\d+")))
                .andExpect(jsonPath("$.error").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Demasiadas peticiones; intente nuevamente más tarde"))
                .andExpect(jsonPath("$.endpoint").value("/api/auth/login"));

        verify(authService, times(2)).login(any());
    }
}
