package com.vbank.bffservice.exception;

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

    @Schema(example = "Failed to retrieve dashboard data due to an issue with downstream services.")
    private String message;
}
