package com.graceconsulting.cardmanagement.service;

import com.graceconsulting.cardmanagement.dto.AuthRequest;
import com.graceconsulting.cardmanagement.dto.AuthResponse;
import com.graceconsulting.cardmanagement.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse authenticate(AuthRequest request) {
        log.info("Tentativa de login: {}", request.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtTokenProvider.generateToken(authentication);
        log.info("Login realizado com sucesso: {}", request.username());

        return new AuthResponse(token, jwtTokenProvider.getExpirationInSeconds());
    }
}
