package com.vbank.userservice.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    @Schema(example = "409")
    private int status;

    @Schema(example = "Conflict")
    private String error;

    @Schema(example = "Username or email already exists.")
    private String message;
}
