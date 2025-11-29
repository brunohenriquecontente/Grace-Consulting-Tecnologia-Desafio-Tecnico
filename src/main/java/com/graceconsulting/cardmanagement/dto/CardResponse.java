package com.graceconsulting.cardmanagement.dto;

import java.time.LocalDateTime;

public record CardResponse(
    Long id,
    String maskedNumber,
    LocalDateTime createdAt
) {}
