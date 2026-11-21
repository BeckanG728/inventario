package com.taller.seguridad.autenticacion.service;

import com.taller.seguridad.autenticacion.dto.request.LoginRequest;
import com.taller.seguridad.autenticacion.dto.request.RegisterRequest;
import com.taller.seguridad.autenticacion.dto.response.LoginResponse;
import com.taller.seguridad.autenticacion.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
