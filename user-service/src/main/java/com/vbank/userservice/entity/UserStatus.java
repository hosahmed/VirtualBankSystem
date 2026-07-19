package com.vbank.userservice.entity;

/**
 * ACTIVE is the only state the spec's endpoints need to check for now (login
 * should reject a
 * SUSPENDED user)
 * but it won't hurt to add it to the schema :)
 */
public enum UserStatus {
    ACTIVE,
    SUSPENDED
}
