package com.taller.config;

import com.taller.alerta.controller.AlertaController;
import com.taller.alerta.service.AlertaService;
import com.taller.auth.security.ErrorResponseWriter;
import com.taller.auth.security.JwtUtil;
import com.taller.auth.security.TokenAuthenticationEntryPoint;
import com.taller.auth.service.UsuarioDetailsService;
import com.taller.dashboard.controller.DashboardController;
import com.taller.dashboard.service.DashboardService;
import com.taller.existencia.controller.ExistenciaController;
import com.taller.existencia.service.ExistenciaService;
import com.taller.producto.controller.ProductoController;
import com.taller.producto.service.ProductoService;
import com.taller.reporte.controller.EstadisticasController;
import com.taller.reporte.controller.ReporteMovimientosController;
import com.taller.reporte.service.EstadisticasService;
import com.taller.reporte.service.ReporteMovimientosService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * T9 — Pruebas de la matriz de acceso de CATALOGO_ENDPOINTS §2 para T4, T5 y T6.
 */
@WebMvcTest(controllers = {
        ProductoController.class,
        ExistenciaController.class,
        AlertaController.class,
        DashboardController.class,
        ReporteMovimientosController.class,
        EstadisticasController.class
})
@Import({SecurityConfig.class, ErrorResponseWriter.class, TokenAuthenticationEntryPoint.class})
@TestPropertySource(properties = {
        "SERVER_PORT=8080",
        "JWT_SECRET=test-secret-at-least-32-characters-long!!",
        "JWT_EXPIRACION_SEGUNDOS=3600",
        "ADMIN_USERNAME=admin",
        "ADMIN_PASSWORD=Password123!",
        "ALERTAS_CRON=0 0 * * * ?"
})
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private ExistenciaService existenciaService;

    @MockitoBean
    private AlertaService alertaService;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private ReporteMovimientosService reporteMovimientosService;

    @MockitoBean
    private EstadisticasService estadisticasService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.Mockito.when(productoService.listar(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(org.springframework.data.domain.Page.empty());
    }

    // --- Sin autenticación -> 401 UNAUTHORIZED ---

    @Test
    void endpointsModificados_sinToken_devuelven401() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/existencias")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/alertas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/reportes/movimientos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/estadisticas")).andExpect(status().isUnauthorized());
    }

    // --- T6: ADMIN excluido de productos, existencias y alertas -> 403 FORBIDDEN ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_consultasInventario_devuelve403ConContrato() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("No tiene permisos para realizar esta acción"))
                .andExpect(jsonPath("$.endpoint").value("/api/productos"));

        mockMvc.perform(get("/api/existencias")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/alertas")).andExpect(status().isForbidden());
    }

    // --- T4 y T5: ADMIN excluido de dashboard, reportes y estadísticas -> 403 FORBIDDEN ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_dashboardYReportes_devuelve403() throws Exception {
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reportes/movimientos")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/estadisticas")).andExpect(status().isForbidden());
    }

    // --- T4 y T5: MECANICO excluido de dashboard, reportes y estadísticas -> 403 FORBIDDEN ---

    @Test
    @WithMockUser(roles = "MECANICO")
    void mecanico_dashboardYReportes_devuelve403() throws Exception {
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reportes/movimientos")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/estadisticas")).andExpect(status().isForbidden());
    }

    // --- T6: MECANICO accede a productos, existencias y alertas -> 200 OK ---

    @Test
    @WithMockUser(roles = "MECANICO")
    void mecanico_consultasInventario_permiteAcceso() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/existencias?ids=1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/alertas")).andExpect(status().isOk());
    }

    // --- T4, T5, T6: JEFE_TALLER accede a todos ---

    @Test
    @WithMockUser(roles = "JEFE_TALLER")
    void jefeTaller_todosLosEndpointsModificados_permiteAcceso() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/existencias?ids=1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/alertas")).andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/api/reportes/movimientos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/estadisticas")).andExpect(status().isOk());
    }
}
