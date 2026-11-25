package com.taller.catalogo.unidad.service;

import com.taller.catalogo.producto.dto.response.ProductoResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Convierte la cantidad introducida por el usuario a unidades base del producto.
 *
 * <p>La unidad base es la única que se almacena en el stock. Las presentaciones (CAJA, METRO, ROLLO)
 * se convierten mediante un factor entero; la longitud introducida en metros puede ser decimal y se
 * transforma a centímetros enteros.</p>
 */
@Component
public class ConversorUnidades {

    public record ResultadoConversion(
            int cantidadBase,
            String unidadRegistro,
            Integer factorAplicado,
            BigDecimal costoUnitarioBase
    ) {
    }

    public ResultadoConversion convertir(ProductoResponse producto,
                                         Integer cantidad,
                                         BigDecimal cantidadExacta,
                                         String unidadRegistro,
                                         Integer factorSolicitado,
                                         BigDecimal costoUnitario) {
        String codigoRegistro = unidadRegistro == null || unidadRegistro.isBlank()
                ? producto.unidadBaseCodigo()
                : unidadRegistro.trim().toUpperCase(java.util.Locale.ROOT);

        int factor = resolverFactor(producto, codigoRegistro, factorSolicitado);

        if (cantidadExacta != null
                && !codigoRegistro.equals("METRO")
                && !codigoRegistro.equals("ROLLO")) {
            throw new IllegalArgumentException(
                    "La cantidad decimal solo se admite en unidades de longitud (METRO o ROLLO)");
        }

        BigDecimal valor = cantidadExacta != null ? cantidadExacta
                : (cantidad != null ? BigDecimal.valueOf(cantidad) : null);
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        BigDecimal base = valor.multiply(BigDecimal.valueOf(factor)).stripTrailingZeros();
        if (base.scale() > 0) {
            throw new IllegalArgumentException("La conversión debe dar un número entero de unidades base");
        }
        if (base.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException("La cantidad convertida excede el máximo permitido");
        }

        BigDecimal costoBase = null;
        if (costoUnitario != null) {
            costoBase = costoUnitario.divide(BigDecimal.valueOf(factor), 6, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new ResultadoConversion(base.intValueExact(), codigoRegistro, factor > 1 ? factor : null, costoBase);
    }

    private int resolverFactor(ProductoResponse producto, String codigoRegistro, Integer factorSolicitado) {
        if (factorSolicitado != null) {
            if (factorSolicitado <= 0) {
                throw new IllegalArgumentException("El factor de presentación debe ser mayor que cero");
            }
            return factorSolicitado;
        }
        String presentacion = producto.unidadPresentacionCodigo();
        if (presentacion != null && presentacion.equalsIgnoreCase(codigoRegistro)) {
            Integer factor = producto.factorPresentacion();
            if (factor == null || factor <= 0) {
                throw new IllegalArgumentException("La presentación " + codigoRegistro + " no tiene un factor válido");
            }
            return factor;
        }
        if (producto.unidadBaseCodigo() != null
                && !producto.unidadBaseCodigo().equalsIgnoreCase(codigoRegistro)) {
            throw new IllegalArgumentException("La unidad " + codigoRegistro
                    + " no es la unidad base ni una presentación del producto; indique el factor");
        }
        return 1;
    }
}
