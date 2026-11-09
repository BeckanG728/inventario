package com.taller.catalogo.proveedor.dto.response;

import com.taller.catalogo.proveedor.entity.Proveedor;

public record ProveedorResponse(Long id, String ruc, String razonSocial, String contacto,
                                String telefono, String correo, String direccion, boolean activo) {

    public static ProveedorResponse from(Proveedor p) {
        return new ProveedorResponse(p.getId(), p.getRuc(), p.getRazonSocial(), p.getContacto(),
                p.getTelefono(), p.getCorreo(), p.getDireccion(), p.isActivo());
    }
}