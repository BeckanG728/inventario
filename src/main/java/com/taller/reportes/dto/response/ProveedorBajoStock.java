package com.taller.reportes.dto.response;

import java.util.List;

public record ProveedorBajoStock(
        String proveedor,
        List<ProductoBajoStock> productos
) {
}