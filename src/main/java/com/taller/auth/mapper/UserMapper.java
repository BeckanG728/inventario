package com.taller.auth.mapper;

import com.taller.auth.dto.response.LoginResponse;
import com.taller.auth.dto.response.RegisterResponse;
import com.taller.auth.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "rol.nombre", target = "role")
    RegisterResponse toRegisterResponse(Usuario usuario);

    @Mapping(source = "usuario.id", target = "userId")
    @Mapping(source = "usuario.rol.nombre", target = "role")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "expiresIn", target = "expiresIn")
    @Mapping(constant = "Bearer", target = "tokenType")
    LoginResponse toLoginResponse(Usuario usuario, String token, long expiresIn);
}
