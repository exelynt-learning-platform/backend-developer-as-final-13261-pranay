package com.Resource_Booking_System.Configure;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final MyUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, MyUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                 http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint((request, response, authException) -> {

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                    response.setContentType("application/json");

                    response.getWriter().write("{\"error\":\"Unauthorized - Invalid or missing token\"}");
                })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                    response.setContentType("application/json");

                    response.getWriter().write("{\"error\":\"Forbidden - Permission denied\"}");
                }))


                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/resources/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/resources/**").hasAuthority("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/resources/**").hasAuthority("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/resources/**").hasAuthority("ADMIN")


                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reservations/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reservations/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/reservations/**").hasAuthority("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/reservations/**").hasAuthority("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/reservations/**").hasAuthority("ADMIN")

                        .anyRequest().authenticated())
                         .authenticationProvider(authenticationProvider())
                         .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}