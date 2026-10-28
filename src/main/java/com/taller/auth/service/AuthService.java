package com.taller.auth.service;

import com.taller.auth.dto.request.LoginRequest;
import com.taller.auth.dto.request.RegisterRequest;
import com.taller.auth.dto.response.LoginResponse;
import com.taller.auth.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
