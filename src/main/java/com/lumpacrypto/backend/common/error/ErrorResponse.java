package com.lumpacrypto.backend.common.error;

import java.time.Instant;

// standard error body -> matches frontend ApiErrorBody {message, code, timestamp}
public record ErrorResponse(String message, String code, Instant timestamp) {

    public static ErrorResponse of(String message, String code) {
        return new ErrorResponse(message, code, Instant.now());
    }
}