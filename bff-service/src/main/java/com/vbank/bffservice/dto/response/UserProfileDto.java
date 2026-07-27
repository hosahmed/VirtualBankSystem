package com.vbank.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Deserialization target for User Service's GET /users/{userId}/profile
 * response. This is an ASSUMPTION about User Service's real contract -
 * it matches the spec's documented example and the actual User Service
 * we built (see user-service/README.md). If your friend's Account/
 * Transaction services are what's uncertain here, this class isn't
 * affected - it's the one downstream contract we know for certain
 * because we built it ourselves.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
