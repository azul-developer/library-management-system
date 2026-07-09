package com.liz.library.application.service;

import com.liz.library.application.dto.LoginRequest;
import com.liz.library.application.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

}