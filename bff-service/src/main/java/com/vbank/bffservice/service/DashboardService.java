package com.vbank.bffservice.service;

import com.vbank.bffservice.dto.response.DashboardResponse;

import java.util.UUID;

public interface DashboardService {
    DashboardResponse getDashboard(UUID userId);
}
