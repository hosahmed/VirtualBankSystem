package com.vbank.userservice.mapper;

import com.vbank.userservice.dto.response.LoginResponse;
import com.vbank.userservice.dto.response.UserProfileResponse;
import com.vbank.userservice.dto.response.UserRegisteredResponse;
import com.vbank.userservice.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserRegisteredResponse toRegisteredResponse(User user) {
        return UserRegisteredResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .message("User registered successfully.")
                .build();
    }

    public LoginResponse toLoginResponse(User user) {
        return LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .build();
    }

    public UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
