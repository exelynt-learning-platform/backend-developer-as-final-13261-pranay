package com.resource_booking_system.Configure;

import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("dev")
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${seed.admin.username}")
    private String adminUsername;

    @Value("${seed.admin.email}")
    private String adminEmail;

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Value("${seed.user.username}")
    private String userUsername;

    @Value("${seed.user.email}")
    private String userEmail;

    @Value("${seed.user.password}")
    private String userPassword;

    @Bean
    @Transactional
    public CommandLineRunner createUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {

        return args -> {

            if (!seedEnabled) {
                logger.info("DataInitializer skipped (app.seed.enabled=false)");
                return;
            }

            logger.warn("DataInitializer is running — do NOT enable app.seed.enabled in production");

            if (!userRepository.existsByUsername(adminUsername)) {

                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);

                userRepository.save(admin);
                logger.info("Seeded ADMIN user '{}'", adminUsername);
            }

            if (!userRepository.existsByUsername(userUsername)) {

                User user = new User();
                user.setUsername(userUsername);
                user.setEmail(userEmail);
                user.setPassword(passwordEncoder.encode(userPassword));
                user.setRole(Role.USER);
                user.setEnabled(true);

                userRepository.save(user);
                logger.info("Seeded USER user '{}'", userUsername);
            }
        };
    }
}