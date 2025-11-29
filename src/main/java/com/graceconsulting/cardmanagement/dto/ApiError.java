package com.graceconsulting.cardmanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
    int status,
    String message,
    List<String> errors,
    LocalDateTime timestamp
) {
    public ApiError(int status, String message) {
        this(status, message, List.of(), LocalDateTime.now());
    }

    public ApiError(int status, String message, List<String> errors) {
        this(status, message, errors, LocalDateTime.now());
    }
}
