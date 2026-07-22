package com.vbank.bffservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    @Schema(example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID userId;

    @Schema(example = "john.doe")
    private String username;

    @Schema(example = "john.doe@example.com")
    private String email;

    @Schema(example = "John")
    private String firstName;

    @Schema(example = "Doe")
    private String lastName;
}
