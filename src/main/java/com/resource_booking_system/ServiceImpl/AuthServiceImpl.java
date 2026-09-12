package com.resource_booking_system.ServiceImpl;

import com.resource_booking_system.Configure.JwtUtil;
import com.resource_booking_system.Dto.*;
import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Exception.DuplicateEmailException;
import com.resource_booking_system.Exception.DuplicateUsernameException;
import com.resource_booking_system.Exception.InvalidCredentialsException;
import com.resource_booking_system.IService.IAuthService;
import com.resource_booking_system.Repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public SignUpResponse signUp(SignUpRequest signUpRequest) {

        String username = signUpRequest.getUsername().trim();
        String email = signUpRequest.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        User created = userRepository.save(user);

        SignUpResponse response = new SignUpResponse();
        response.setId(created.getId());
        response.setUsername(created.getUsername());
        response.setEmail(created.getEmail());
        response.setRole(created.getRole().name());

        return response;
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException ex) {

            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = jwtUtil.generateAccessToken(loginRequest.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getUsername());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);

        return authResponse;
    }

    @Override
    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        Claims claims;
        try {
            claims = jwtUtil.extractAllClaims(refreshToken);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        if (!jwtUtil.validateRefreshToken(claims)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        String username = jwtUtil.extractUsername(claims);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new InvalidCredentialsException("User no longer exists"));

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("User account is disabled");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(newAccessToken);
        authResponse.setRefreshToken(newRefreshToken);

        return authResponse;
    }
}