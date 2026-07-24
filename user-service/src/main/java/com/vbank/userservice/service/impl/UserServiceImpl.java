package com.vbank.userservice.service.impl;

import com.vbank.userservice.dto.request.LoginRequest;
import com.vbank.userservice.dto.request.RegisterRequest;
import com.vbank.userservice.dto.response.LoginResponse;
import com.vbank.userservice.dto.response.UserProfileResponse;
import com.vbank.userservice.dto.response.UserRegisteredResponse;
import com.vbank.userservice.entity.User;
import com.vbank.userservice.entity.UserStatus;
import com.vbank.userservice.exception.InvalidCredentialsException;
import com.vbank.userservice.exception.UserAlreadyExistsException;
import com.vbank.userservice.exception.UserNotFoundException;
import com.vbank.userservice.mapper.UserMapper;
import com.vbank.userservice.repository.UserRepository;
import com.vbank.userservice.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * All business logic lives here.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserRegisteredResponse register(RegisterRequest request) {
        // Gives a fast, friendly 409 in the common (non-race) case.
        // The DB unique constraint (see User entity)
        // is the real guarantee against concurrent
        // duplicate inserts.
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username or email already exists.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Username or email already exists.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        return userMapper.toRegisteredResponse(saved);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        return userMapper.toLoginResponse(user);
    }



    @Override
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with ID " + userId + " not found."));
        return userMapper.toProfileResponse(user);
    }
}
