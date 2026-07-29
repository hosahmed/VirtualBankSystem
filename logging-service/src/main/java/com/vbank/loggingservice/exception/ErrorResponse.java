package com.vbank.loggingservice.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    @Schema(example = "500")
    private int status;

    @Schema(example = "Internal Server Error")
    private String error;

    @Schema(example = "An unexpected error occurred.")
    private String message;
}
