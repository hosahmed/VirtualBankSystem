package com.vbank.bffservice.controller;

import com.vbank.bffservice.dto.response.DashboardResponse;
import com.vbank.bffservice.exception.ErrorResponse;
import com.vbank.bffservice.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/bff")
@Tag(name = "BFF", description = "Aggregated endpoints for the frontend - "
        + "the only place in this system that calls multiple other "
        + "microservices directly")
public class BffController {

    private final DashboardService dashboardService;

    public BffController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/{userId}")
    @Operation(summary = "Get the aggregated dashboard for a user",
            description = "Combines profile (User Service), accounts "
                    + "(Account Service), and each account's transaction "
                    + "history (Transaction Service) into a single response. "
                    + "Account and transaction lookups run in parallel.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard assembled successfully",
                    content = @Content(schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "404", description = "No user exists with the given ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "A downstream service (User/Account/Transaction) "
                    + "failed or timed out during aggregation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DashboardResponse> getDashboard(
            @Parameter(description = "ID of the user whose dashboard is being requested")
            @PathVariable UUID userId) {
        DashboardResponse response = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(response);
    }
}
