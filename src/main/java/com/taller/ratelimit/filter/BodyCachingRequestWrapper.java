package com.taller.ratelimit.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Permite leer el cuerpo en el filtro y seguir entregándolo al controlador. */
final class BodyCachingRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cuerpo;

    BodyCachingRequestWrapper(HttpServletRequest request, byte[] cuerpo) {
        super(request);
        this.cuerpo = cuerpo;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream entrada = new ByteArrayInputStream(cuerpo);
        return new ServletInputStream() {
            @Override
            public int read() {
                return entrada.read();
            }

            @Override
            public boolean isFinished() {
                return entrada.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // El cuerpo ya está en memoria; no se necesita lectura asíncrona.
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
}
