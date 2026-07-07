package com.examp.genifit.config;

import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserRole;
import com.examp.genifit.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            String adminUsername = "admin";
            String adminEmail = "admin@genifit.com";

            boolean adminExists =
                    userRepository.existsByUsername(adminUsername)
                            || userRepository.existsByEmail(adminEmail);

            if (adminExists) {
                log.info("Default admin already exists. Skipping creation.");
                return;
            }

            User user = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("admin"))
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();

            userRepository.save(user);

            log.warn("Admin user has been created with default password: admin, please change it");
        };
    }
}