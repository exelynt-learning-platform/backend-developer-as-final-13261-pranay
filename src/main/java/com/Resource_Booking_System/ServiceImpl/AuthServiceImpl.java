package com.Resource_Booking_System.ServiceImpl;

import com.Resource_Booking_System.Configure.JwtUtil;
import com.Resource_Booking_System.Dto.AuthResponse;
import com.Resource_Booking_System.Dto.LoginRequest;
import com.Resource_Booking_System.Dto.SignUpRequest;
import com.Resource_Booking_System.Dto.SignUpResponse;
import com.Resource_Booking_System.Entity.User;
import com.Resource_Booking_System.Exception.UserEmailNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.Resource_Booking_System.IService.IAuthService;
import com.Resource_Booking_System.Repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

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
            throw new UsernameNotFoundException("UserName AlReady Exists");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new UserEmailNotFoundException("Email AlReady Exists");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));


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

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        String accesToken = jwtUtil.generateAccessToken(loginRequest.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getUsername());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accesToken);
        authResponse.setRefreshToken(refreshToken);

        return authResponse;
    }
}
