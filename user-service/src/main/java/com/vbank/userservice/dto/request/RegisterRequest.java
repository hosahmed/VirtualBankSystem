package com.vbank.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(example = "john.doe", description = "Unique username")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Schema(example = "securePassword123", description = "Password (will be BCrypt-hashed)")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(example = "john.doe@example.com", description = "Email address")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    @Schema(example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    @Schema(example = "Doe")
    private String lastName;

    @Schema(example = "ROLE_USER", description = "User role (ROLE_USER or ROLE_ADMIN)")
    private String role;
}
