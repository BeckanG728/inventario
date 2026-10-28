package com.taller.auth.security;

import com.taller.auth.entity.Rol;
import com.taller.auth.entity.Usuario;
import com.taller.auth.service.UsuarioDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UsuarioDetailsService usuarioDetailsService;
    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtUtil, usuarioDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void missingAuthorization_continuesUnauthenticated() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(request.getAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void validToken_setsAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer good-token");
        Claims claims = Jwts.claims().subject("1").build();
        Usuario usuario = usuario(1L, true);

        when(jwtUtil.parse("good-token")).thenReturn(claims);
        when(jwtUtil.extractUserId(claims)).thenReturn(1L);
        when(usuarioDetailsService.loadUserByUserID(1L)).thenReturn(usuario);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void disabledUser_setsAccountDisabled() throws Exception {
        request.addHeader("Authorization", "Bearer good-token");
        Claims claims = Jwts.claims().subject("1").build();
        Usuario usuario = usuario(1L, false);

        when(jwtUtil.parse("good-token")).thenReturn(claims);
        when(jwtUtil.extractUserId(claims)).thenReturn(1L);
        when(usuarioDetailsService.loadUserByUserID(1L)).thenReturn(usuario);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(TokenAuthenticationEntryPoint.ACCOUNT_DISABLED,
                request.getAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE));
    }

    @Test
    void expiredToken_setsTokenExpired() throws Exception {
        request.addHeader("Authorization", "Bearer expired");
        when(jwtUtil.parse(anyString())).thenThrow(new ExpiredJwtException(null, null, "expired"));

        filter.doFilter(request, response, filterChain);

        assertEquals(TokenAuthenticationEntryPoint.TOKEN_EXPIRED,
                request.getAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidToken_setsTokenInvalid() throws Exception {
        request.addHeader("Authorization", "Bearer bad");
        when(jwtUtil.parse(anyString())).thenThrow(new MalformedJwtException("bad"));

        filter.doFilter(request, response, filterChain);

        assertEquals(TokenAuthenticationEntryPoint.TOKEN_INVALID,
                request.getAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE));
    }

    @Test
    void unknownUser_setsTokenInvalid() throws Exception {
        request.addHeader("Authorization", "Bearer good-token");
        Claims claims = Jwts.claims().subject("99").build();
        when(jwtUtil.parse("good-token")).thenReturn(claims);
        when(jwtUtil.extractUserId(claims)).thenReturn(99L);
        when(usuarioDetailsService.loadUserByUserID(99L))
                .thenThrow(new UsernameNotFoundException("gone"));

        filter.doFilter(request, response, filterChain);

        assertEquals(TokenAuthenticationEntryPoint.TOKEN_INVALID,
                request.getAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private Usuario usuario(Long id, boolean activo) {
        Usuario usuario = new Usuario("carlos", "hashed", new Rol("MECANICO"));
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setActivo(activo);
        return usuario;
    }
}
