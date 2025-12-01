package com.graceconsulting.cardmanagement.config;

import com.graceconsulting.cardmanagement.entity.User;
import com.graceconsulting.cardmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user"))
                    .name("Default User")
                    .active(true)
                    .build();

            userRepository.save(user);
            log.info("Default user 'user' created successfully");
        } else {
            log.info("Default user 'user' already exists");
        }
    }
}
