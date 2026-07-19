package com.vbank.userservice.controller;

import com.vbank.userservice.dto.request.LoginRequest;
import com.vbank.userservice.dto.request.RegisterRequest;
import com.vbank.userservice.dto.response.LoginResponse;
import com.vbank.userservice.dto.response.UserProfileResponse;
import com.vbank.userservice.dto.response.UserRegisteredResponse;
import com.vbank.userservice.exception.ErrorResponse;
import com.vbank.userservice.exception.ForbiddenException;
import com.vbank.userservice.security.GatewayAuthInterceptor;
import com.vbank.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with a hashed password.")
    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserRegisteredResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<UserRegisteredResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserRegisteredResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Authenticate a user", description = "Validates credentials and returns user info on success.")
    @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user profile", description = "Returns profile details for the authenticated user.")
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, required = true, description = "Authenticated user ID (forwarded by WSO2 Gateway)", schema = @Schema(type = "string", format = "uuid", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"))
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserProfileResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid X-User-Id header", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Authenticated user does not match requested profile", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @Parameter(description = "Target user ID") @PathVariable UUID userId,
            HttpServletRequest request) {
        UUID authenticatedUserId = (UUID) request.getAttribute(GatewayAuthInterceptor.AUTHENTICATED_USER_ID_ATTR);
        if (!userId.equals(authenticatedUserId)) {
            throw new ForbiddenException("You are not authorized to view this profile.");
        }

        UserProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
}
