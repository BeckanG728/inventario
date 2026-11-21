package com.taller.seguridad.usuario.mapper;

import com.taller.seguridad.autenticacion.dto.response.LoginResponse;
import com.taller.seguridad.autenticacion.dto.response.RegisterResponse;
import com.taller.seguridad.usuario.dto.response.UsuarioResponse;
import com.taller.seguridad.usuario.entity.Usuario;
import com.taller.seguridad.autenticacion.security.DetallesUsuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "rol.nombre", target = "role")
    RegisterResponse toRegisterResponse(Usuario usuario);

    @Mapping(source = "rol.nombre", target = "role")
    UsuarioResponse toUsuarioResponse(Usuario usuario);

    @Mapping(source = "usuario.id", target = "userId")
    @Mapping(source = "usuario.rol.nombre", target = "role")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "expiresIn", target = "expiresIn")
    @Mapping(constant = "Bearer", target = "tokenType")
    LoginResponse toLoginResponse(Usuario usuario, String token, long expiresIn);

    @Mapping(source = "detalles.id", target = "userId")
    @Mapping(source = "detalles.role", target = "role")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "expiresIn", target = "expiresIn")
    @Mapping(constant = "Bearer", target = "tokenType")
    LoginResponse toLoginResponse(DetallesUsuario detalles, String token, long expiresIn);
}
