package com.graceconsulting.cardmanagement.service;

import com.graceconsulting.cardmanagement.dto.UserRegisterRequest;
import com.graceconsulting.cardmanagement.entity.User;
import com.graceconsulting.cardmanagement.exception.ResourceConflictException;
import com.graceconsulting.cardmanagement.mapper.UserMapper;
import com.graceconsulting.cardmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @Transactional
    public User register(UserRegisterRequest request) {
        log.info("Registrando novo usuário: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceConflictException("Username já está em uso");
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        log.info("Usuário registrado com sucesso: {}", savedUser.getUsername());
        return savedUser;
    }
}
