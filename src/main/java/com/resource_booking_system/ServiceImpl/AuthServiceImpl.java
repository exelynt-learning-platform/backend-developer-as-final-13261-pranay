package com.resource_booking_system.ServiceImpl;

import com.resource_booking_system.Configure.JwtUtil;
import com.resource_booking_system.Dto.*;
import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Exception.UserEmailNotFoundException;
import com.resource_booking_system.IService.IAuthService;
import com.resource_booking_system.Repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UsernameNotFoundException("Username Already Exists");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new UserEmailNotFoundException("Email Already Exists");
        }

        User user = new User();

        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.USER);

        User create = userRepository.save(user);

        SignUpResponse response = new SignUpResponse();

        response.setId(create.getId());
        response.setUsername(create.getUsername());
        response.setEmail(create.getEmail());
        response.setRole(create.getRole().name());

        return response;
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

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

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(username);

        AuthResponse authResponse = new AuthResponse();

        authResponse.setAccessToken(newAccessToken);
        authResponse.setRefreshToken(refreshToken);

        return authResponse;
    }
}