package com.taller.auth.security;

import com.taller.auth.entity.Usuario;
import com.taller.auth.service.UsuarioDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = jwtUtil.parse(token);
            Long userId = jwtUtil.extractUserId(claims);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Usuario usuario = usuarioDetailsService.loadUserByUserID(userId);

                if (!usuario.isEnabled()) {
                    log.warn("Usuario inactivo — URI: {}", request.getRequestURI());
                    reject(request, TokenAuthenticationEntryPoint.ACCOUNT_DISABLED);
                } else {
                    UsernamePasswordAuthenticationToken auth =
                            UsernamePasswordAuthenticationToken.authenticated(
                                    usuario,
                                    null,
                                    usuario.getAuthorities()
                            );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (ExpiredJwtException e) {
            reject(request, TokenAuthenticationEntryPoint.TOKEN_EXPIRED);
        } catch (UsernameNotFoundException e) {
            log.warn("Usuario no encontrado — URI: {}", request.getRequestURI());
            reject(request, TokenAuthenticationEntryPoint.TOKEN_INVALID);
        } catch (JwtException | IllegalArgumentException e) {
            reject(request, TokenAuthenticationEntryPoint.TOKEN_INVALID);
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, String error) {
        request.setAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE, error);
        SecurityContextHolder.clearContext();
    }
}
