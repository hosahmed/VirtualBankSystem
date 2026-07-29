package com.vbank.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * This class - not UserProfileDto/AccountDto/TransactionDto - is the
 * BFF's OWN public contract, the thing the frontend/gateway actually
 * depends on. The Dto classes above are internal deserialization
 * targets for calls to other services and could change shape without
 * this class changing, as long as the mapper absorbs the difference.
 * That boundary is the whole point of the BFF pattern.
 */
@Getter
@Builder
@AllArgsConstructor
public class DashboardResponse {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<AccountWithTransactionsDto> accounts;
}
