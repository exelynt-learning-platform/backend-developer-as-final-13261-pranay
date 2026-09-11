package com.Resource_Booking_System.Configure;

import com.Resource_Booking_System.Entity.Role;
import com.Resource_Booking_System.Entity.User;
import com.Resource_Booking_System.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Value("${seed.user.password}")
    private String userPassword;

    @Bean
    public CommandLineRunner createUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepository.existsByUsername("Pranay")) {

                User admin = new User();

                admin.setUsername("Pranay");
                admin.setEmail("pranay@gmail.com");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }

            if (!userRepository.existsByUsername("Ram")) {

                User user = new User();

                user.setUsername("Ram");
                user.setEmail("ram@gmail.com");
                user.setPassword(passwordEncoder.encode(userPassword));
                user.setRole(Role.USER);

                userRepository.save(user);
            }
        };
    }
}