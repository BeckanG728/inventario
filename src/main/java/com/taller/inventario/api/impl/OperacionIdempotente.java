package com.taller.inventario.api.impl;

import com.taller.inventario.error.ConflictoIdempotenciaException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OperacionIdempotente {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    @Transactional
    public <T> T ejecutar(Long usuarioId, String operacion, UUID clave, Object solicitud,
                          Class<T> tipoRespuesta, Supplier<T> ejecutarOperacion) {
        if (clave == null) {
            return ejecutarOperacion.get();
        }

        String huella = huella(solicitud);
        int insertadas = jdbc.update("""
                INSERT INTO operacion_idempotente (usuario_id, operacion, clave, huella)
                VALUES (?, ?, ?, ?) ON CONFLICT (usuario_id, operacion, clave) DO NOTHING
                """, usuarioId, operacion, clave, huella);
        if (insertadas == 0) {
            return jdbc.queryForObject("""
                    SELECT huella, respuesta FROM operacion_idempotente
                    WHERE usuario_id = ? AND operacion = ? AND clave = ?
                    """, (fila, numero) -> {
                if (!huella.equals(fila.getString("huella"))) {
                    throw new ConflictoIdempotenciaException();
                }
                return json.readValue(fila.getString("respuesta"), tipoRespuesta);
            }, usuarioId, operacion, clave);
        }

        T resultado = ejecutarOperacion.get();
        jdbc.update("""
                UPDATE operacion_idempotente SET respuesta = ?
                WHERE usuario_id = ? AND operacion = ? AND clave = ?
                """, json.writeValueAsString(resultado), usuarioId, operacion, clave);
        return resultado;
    }

    private String huella(Object solicitud) {
        try {
            byte[] datos = json.writeValueAsBytes(solicitud);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(datos));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
