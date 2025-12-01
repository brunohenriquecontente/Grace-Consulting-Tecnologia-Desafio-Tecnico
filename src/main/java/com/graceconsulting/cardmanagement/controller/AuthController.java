package com.graceconsulting.cardmanagement.controller;

import com.graceconsulting.cardmanagement.dto.AuthRequest;
import com.graceconsulting.cardmanagement.dto.AuthResponse;
import com.graceconsulting.cardmanagement.dto.UserRegisterRequest;
import com.graceconsulting.cardmanagement.service.AuthService;
import com.graceconsulting.cardmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "authentication", description = "Endpoints de autenticação e registro")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "authenticate-user", description = "Realiza login e retorna token JWT")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.authenticate(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "register-user", description = "Cria um novo usuário no sistema")
    public void register(@Valid @RequestBody UserRegisterRequest request) {
        userService.register(request);
    }
}
