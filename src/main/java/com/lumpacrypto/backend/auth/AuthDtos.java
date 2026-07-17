package com.lumpacrypto.backend.auth;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String username,
            @NotBlank @Size(min = 8, max = 100) String password) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    // matches frontend AuthResponse in types.ts exactly
    public record AuthResponse(
            String token,
            UUID userId,
            String email,
            String username,
            BigDecimal walletBalance,
            Instant expiresAt) {}
}