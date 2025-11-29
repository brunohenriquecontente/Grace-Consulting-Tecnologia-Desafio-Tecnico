package com.graceconsulting.cardmanagement.dto;

public record AuthResponse(
    String token,
    String type,
    Long expiresIn
) {
    public AuthResponse(String token, Long expiresIn) {
        this(token, "Bearer", expiresIn);
    }
}
