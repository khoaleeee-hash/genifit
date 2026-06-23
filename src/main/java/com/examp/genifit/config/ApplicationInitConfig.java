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
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args -> {
            if (!userRepository.existsByUsername("admin")){
                User user = User.builder()
                        .username("admin")
                        .email("admin@genifit.com")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .role(UserRole.ADMIN)
                        .isActive(true)
                        .build();
                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            } else {
                log.info("Admin user already exists. Skipping creation.");
            }
        };
    }
}