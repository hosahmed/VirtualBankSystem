package com.vbank.userservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    @Schema(example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID userId;

    @Schema(example = "john.doe")
    private String username;
}
