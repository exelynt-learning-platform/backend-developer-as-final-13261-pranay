package com.Resource_Booking_System.Configure;

import com.Resource_Booking_System.Entity.Role;
import com.Resource_Booking_System.Entity.User;
import com.Resource_Booking_System.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder)
    {

        return args -> {

            // Create ADMIN
            if (!userRepository.existsByUsername("Pranay")) {

                User admin = new User();

                admin.setUsername("Pranay");
                admin.setEmail("pranay@gmail.com");
                admin.setPassword(passwordEncoder.encode("Pranay123"));
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }


            // Create USER
            if (!userRepository.existsByUsername("Ram")) {

                User user = new User();

                user.setUsername("Ram");
                user.setEmail("ram@gmail.com");
                user.setPassword(passwordEncoder.encode("Ram123"));
                user.setRole(Role.USER);

                userRepository.save(user);
            }
        };
    }
}