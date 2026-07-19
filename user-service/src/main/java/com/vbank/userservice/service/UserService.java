package com.vbank.userservice.service;

import com.vbank.userservice.dto.request.LoginRequest;
import com.vbank.userservice.dto.request.RegisterRequest;
import com.vbank.userservice.dto.response.LoginResponse;
import com.vbank.userservice.dto.response.UserProfileResponse;
import com.vbank.userservice.dto.response.UserRegisteredResponse;

import java.util.UUID;

public interface UserService {

    UserRegisteredResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserProfileResponse getProfile(UUID userId);
}
