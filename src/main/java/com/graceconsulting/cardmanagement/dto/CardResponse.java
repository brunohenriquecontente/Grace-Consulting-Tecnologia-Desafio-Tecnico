package com.graceconsulting.cardmanagement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardResponse(
    UUID id,
    String maskedNumber,
    LocalDateTime createdAt
) {}
