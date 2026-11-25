package com.taller.configuracion;

import com.taller.catalogo.producto.controller.ProductoController;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.producto.service.ProductoService;
import com.taller.monitoreo.service.AlertaService;
import com.taller.monitoreo.controller.MonitoreoController;
import com.taller.catalogo.stockminimo.controller.StockMinimoController;
import com.taller.catalogo.stockminimo.service.StockMinimoService;
import com.taller.monitoreo.service.ExistenciaService;
import com.taller.reportes.consulta.controller.ReporteController;
import com.taller.reportes.consulta.service.InventarioValorService;
import com.taller.reportes.exportacion.service.ReporteExportService;
import com.taller.inventario.api.impl.OperacionIdempotente;
import com.taller.inventario.movimiento.controller.MovimientoController;
import com.taller.inventario.movimiento.service.MovimientoService;
import com.taller.inventario.movimiento.service.ReporteMovimientosService;
import com.taller.reportes.dashboard.controller.DashboardController;
import com.taller.reportes.dashboard.service.DashboardService;
import com.taller.reportes.estadisticas.controller.EstadisticasController;
import com.taller.reportes.estadisticas.service.EstadisticasService;
import com.taller.seguridad.usuario.service.UsuarioDetailsService;
import com.taller.seguridad.ratelimit.config.RateLimitProperties;
import com.taller.seguridad.ratelimit.store.InMemoryRateLimitStore;
import com.taller.seguridad.autenticacion.security.ErrorResponseWriter;
import com.taller.seguridad.autenticacion.security.JwtUtil;
import com.taller.seguridad.autenticacion.security.TokenAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * T9 — Pruebas de la matriz de acceso de CATALOGO_ENDPOINTS §2 para T4, T5 y T6.
 */
@WebMvcTest(controllers = {
        ProductoController.class,
        MonitoreoController.class,
        StockMinimoController.class,
        DashboardController.class,
        MovimientoController.class,
        ReporteController.class,
        EstadisticasController.class
})
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
    private StockMinimoService stockMinimoService;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private MovimientoService movimientoService;

    @MockitoBean
    private OperacionIdempotente operacionIdempotente;

    @MockitoBean
    private ReporteMovimientosService reporteMovimientosService;

    @MockitoBean
    private InventarioValorService inventarioValorService;

    @MockitoBean
    private ReporteExportService reporteExportService;

    @MockitoBean
    private EstadisticasService estadisticasService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.Mockito.when(productoService.listar(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any())).thenReturn(org.springframework.data.domain.Page.empty());
        org.mockito.Mockito.when(existenciaService.listarActivos(
                org.mockito.ArgumentMatchers.any())).thenReturn(org.springframework.data.domain.Page.empty());
    }

    // --- Sin autenticación -> 401 UNAUTHORIZED ---

    @Test
    void endpointsModificados_sinToken_devuelven401() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/monitoreo/existencias")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/monitoreo/alertas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/movimientos")).andExpect(status().isUnauthorized());
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

        mockMvc.perform(get("/api/monitoreo/existencias")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/monitoreo/alertas")).andExpect(status().isForbidden());
    }

    // --- T4 y T5: ADMIN excluido de dashboard, reportes y estadísticas -> 403 FORBIDDEN ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_dashboardYReportes_devuelve403() throws Exception {
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/movimientos")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/estadisticas")).andExpect(status().isForbidden());
    }

    // --- T4 y T5: MECANICO excluido de dashboard, reportes y estadísticas -> 403 FORBIDDEN ---

    @Test
    @WithMockUser(roles = "MECANICO")
    void mecanico_dashboardYReportes_devuelve403() throws Exception {
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/movimientos")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/estadisticas")).andExpect(status().isForbidden());
    }

    // --- T6: MECANICO accede a productos, alertas y existencias -> 200 OK ---

    @Test
    @WithMockUser(roles = "MECANICO")
    void mecanico_consultasInventario() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/monitoreo/alertas")).andExpect(status().isOk());
        mockMvc.perform(get("/api/monitoreo/existencias")).andExpect(status().isOk());
    }

    // --- T4, T5, T6: JEFE_TALLER accede a todos ---

    @Test
    @WithMockUser(roles = "JEFE_TALLER")
    void jefeTaller_todosLosEndpointsModificados_permiteAcceso() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/monitoreo/existencias")).andExpect(status().isOk());
        mockMvc.perform(get("/api/monitoreo/alertas")).andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/api/movimientos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/estadisticas")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void mecanico_stockMinimo_devuelve403() throws Exception {
        mockMvc.perform(put("/api/productos/1/stock-minimo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoStockMinimo\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "JEFE_TALLER")
    void jefeTaller_stockMinimo_devuelve200() throws Exception {
        org.mockito.Mockito.when(stockMinimoService.actualizarStockMinimo(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(new ProductoResponse(1L, null, null, null, null,
                        0, 5, false, null, true, null, null, null, null));

        mockMvc.perform(put("/api/productos/1/stock-minimo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoStockMinimo\":5}"))
                .andExpect(status().isOk());
    }
}
