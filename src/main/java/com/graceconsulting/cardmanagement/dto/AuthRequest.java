package com.graceconsulting.cardmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank(message = "Username é obrigatório")
    String username,

    @NotBlank(message = "Password é obrigatório")
    String password
) {}
