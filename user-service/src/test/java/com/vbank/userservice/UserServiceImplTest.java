package com.vbank.userservice;

import com.vbank.userservice.dto.request.LoginRequest;
import com.vbank.userservice.dto.request.RegisterRequest;
import com.vbank.userservice.dto.response.LoginResponse;
import com.vbank.userservice.dto.response.UserRegisteredResponse;
import com.vbank.userservice.entity.User;
import com.vbank.userservice.entity.UserStatus;
import com.vbank.userservice.exception.InvalidCredentialsException;
import com.vbank.userservice.exception.UserAlreadyExistsException;
import com.vbank.userservice.mapper.UserMapper;
import com.vbank.userservice.repository.UserRepository;
import com.vbank.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private UserMapper userMapper;

        private UserServiceImpl userService;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                userService = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
        }

        @Test
        void register_shouldHashPasswordAndSaveUser_whenUsernameAndEmailAreUnique() {
                RegisterRequest request = new RegisterRequest(
                                "john.doe", "securePassword123", "john.doe@example.com", "John", "Doe");

                when(userRepository.existsByUsername("john.doe")).thenReturn(false);
                when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
                when(passwordEncoder.encode("securePassword123")).thenReturn("hashed-password");

                User savedUser = User.builder()
                                .userId(UUID.randomUUID())
                                .username("john.doe")
                                .email("john.doe@example.com")
                                .passwordHash("hashed-password")
                                .firstName("John")
                                .lastName("Doe")
                                .status(UserStatus.ACTIVE)
                                .build();
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                UserRegisteredResponse expectedResponse = UserRegisteredResponse.builder()
                                .userId(savedUser.getUserId())
                                .username("john.doe")
                                .message("User registered successfully.")
                                .build();
                when(userMapper.toRegisteredResponse(savedUser)).thenReturn(expectedResponse);

                UserRegisteredResponse response = userService.register(request);

                assertThat(response.getUsername()).isEqualTo("john.doe");
                assertThat(response.getMessage()).isEqualTo("User registered successfully.");
                // Confirms we never persist the raw password - only the hash.
                verify(userRepository).save(argThatPasswordIsHashed());
        }

        @Test
        void register_shouldThrowConflict_whenUsernameAlreadyExists() {
                RegisterRequest request = new RegisterRequest(
                                "john.doe", "securePassword123", "new@example.com", "John", "Doe");

                when(userRepository.existsByUsername("john.doe")).thenReturn(true);

                assertThatThrownBy(() -> userService.register(request))
                                .isInstanceOf(UserAlreadyExistsException.class)
                                .hasMessageContaining("already exists");
        }

        private User argThatPasswordIsHashed() {
                return org.mockito.ArgumentMatchers.argThat(user -> "hashed-password".equals(user.getPasswordHash()));
        }

        // ---- login() ----

        @Test
        void login_shouldReturnLoginResponse_whenCredentialsAreValid() {
                LoginRequest request = new LoginRequest("john.doe", "securePassword123");

                User user = User.builder()
                                .userId(UUID.randomUUID())
                                .username("john.doe")
                                .passwordHash("hashed-password")
                                .status(UserStatus.ACTIVE)
                                .build();

                when(userRepository.findByUsername("john.doe")).thenReturn(java.util.Optional.of(user));
                when(passwordEncoder.matches("securePassword123", "hashed-password")).thenReturn(true);

                LoginResponse expected = LoginResponse.builder()
                                .userId(user.getUserId())
                                .username("john.doe")
                                .build();
                when(userMapper.toLoginResponse(user)).thenReturn(expected);

                LoginResponse response = userService.login(request);

                assertThat(response.getUsername()).isEqualTo("john.doe");
        }

        @Test
        void login_shouldThrowInvalidCredentials_whenUsernameDoesNotExist() {
                LoginRequest request = new LoginRequest("ghost", "anyPassword123");

                when(userRepository.findByUsername("ghost")).thenReturn(java.util.Optional.empty());

                assertThatThrownBy(() -> userService.login(request))
                                .isInstanceOf(InvalidCredentialsException.class)
                                .hasMessageContaining("Invalid username or password.");
        }

        @Test
        void login_shouldThrowInvalidCredentials_whenPasswordDoesNotMatch() {
                LoginRequest request = new LoginRequest("john.doe", "wrongPassword");

                User user = User.builder()
                                .userId(UUID.randomUUID())
                                .username("john.doe")
                                .passwordHash("hashed-password")
                                .status(UserStatus.ACTIVE)
                                .build();

                when(userRepository.findByUsername("john.doe")).thenReturn(java.util.Optional.of(user));
                when(passwordEncoder.matches("wrongPassword", "hashed-password")).thenReturn(false);

                assertThatThrownBy(() -> userService.login(request))
                                .isInstanceOf(InvalidCredentialsException.class)
                                .hasMessageContaining("Invalid username or password.");
        }

        @Test
        void login_shouldThrowInvalidCredentials_whenUserIsNotActive() {
                LoginRequest request = new LoginRequest("john.doe", "securePassword123");

                User user = User.builder()
                                .userId(UUID.randomUUID())
                                .username("john.doe")
                                .passwordHash("hashed-password")
                                .status(UserStatus.SUSPENDED)
                                .build();

                when(userRepository.findByUsername("john.doe")).thenReturn(java.util.Optional.of(user));
                when(passwordEncoder.matches("securePassword123", "hashed-password")).thenReturn(true);

                assertThatThrownBy(() -> userService.login(request))
                                .isInstanceOf(InvalidCredentialsException.class)
                                .hasMessageContaining("Invalid username or password.");
        }

        // ---- getProfile() ----

        @Test
        void getProfile_shouldReturnProfile_whenUserExists() {
                UUID userId = UUID.randomUUID();
                User user = User.builder()
                                .userId(userId)
                                .username("john.doe")
                                .email("john.doe@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .status(UserStatus.ACTIVE)
                                .build();

                when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

                com.vbank.userservice.dto.response.UserProfileResponse expected = com.vbank.userservice.dto.response.UserProfileResponse
                                .builder()
                                .userId(userId)
                                .username("john.doe")
                                .email("john.doe@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .build();
                when(userMapper.toProfileResponse(user)).thenReturn(expected);

                com.vbank.userservice.dto.response.UserProfileResponse response = userService.getProfile(userId);

                assertThat(response.getUsername()).isEqualTo("john.doe");
                assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        void getProfile_shouldThrowUserNotFound_whenUserDoesNotExist() {
                UUID userId = UUID.randomUUID();
                when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

                assertThatThrownBy(() -> userService.getProfile(userId))
                                .isInstanceOf(com.vbank.userservice.exception.UserNotFoundException.class)
                                .hasMessageContaining(userId.toString());
        }
}
