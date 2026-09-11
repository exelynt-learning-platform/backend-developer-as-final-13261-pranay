package com.Resource_Booking_System.Service;

import com.Resource_Booking_System.Configure.JwtUtil;
import com.Resource_Booking_System.Dto.AuthResponse;
import com.Resource_Booking_System.Dto.LoginRequest;
import com.Resource_Booking_System.Dto.SignUpRequest;
import com.Resource_Booking_System.Dto.SignUpResponse;
import com.Resource_Booking_System.Entity.Role;
import com.Resource_Booking_System.Entity.User;
import com.Resource_Booking_System.Exception.UserEmailNotFoundException;
import com.Resource_Booking_System.Repository.UserRepository;
import com.Resource_Booking_System.ServiceImpl.AuthServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;


    // ---------------------------------------------------------
    // SIGNUP - SUCCESS
    // ---------------------------------------------------------

    @Test
    void signUp_ShouldCreateUserSuccessfully() {

        SignUpRequest request = new SignUpRequest();

        request.setUsername("Rahul");
        request.setEmail("rahul@gmail.com");
        request.setPassword("Rahul123");


        when(userRepository.existsByUsername("Rahul")).thenReturn(false);

        when(userRepository.existsByEmail("rahul@gmail.com")).thenReturn(false);

        when(passwordEncoder.encode("Rahul123")).thenReturn("encodedPassword");


        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setUsername("Rahul");
        savedUser.setEmail("rahul@gmail.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.USER);


        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        SignUpResponse response = authService.signUp(request);


        assertNotNull(response);

        assertEquals(1L, response.getId());
        assertEquals("Rahul", response.getUsername());
        assertEquals("rahul@gmail.com", response.getEmail());
        assertEquals("USER", response.getRole());


        verify(userRepository).existsByUsername("Rahul");

        verify(userRepository).existsByEmail("rahul@gmail.com");

        verify(passwordEncoder).encode("Rahul123");

        verify(userRepository).save(any(User.class));
    }


    // ---------------------------------------------------------
    // SIGNUP - USERNAME ALREADY EXISTS
    // ---------------------------------------------------------

    @Test
    void signUp_WhenUsernameExists_ShouldThrowException() {

        SignUpRequest request = new SignUpRequest();

        request.setUsername("Rahul");
        request.setEmail("rahul@gmail.com");
        request.setPassword("Rahul123");


        when(userRepository.existsByUsername("Rahul")).thenReturn(true);


        assertThrows(UsernameNotFoundException.class, () -> authService.signUp(request));


        verify(userRepository).existsByUsername("Rahul");

        verify(userRepository, never()).save(any(User.class));
    }


    // ---------------------------------------------------------
    // SIGNUP - EMAIL ALREADY EXISTS
    // ---------------------------------------------------------

    @Test
    void signUp_WhenEmailExists_ShouldThrowException() {

        SignUpRequest request = new SignUpRequest();

        request.setUsername("Rahul");
        request.setEmail("rahul@gmail.com");
        request.setPassword("Rahul123");


        when(userRepository.existsByUsername("Rahul")).thenReturn(false);

        when(userRepository.existsByEmail("rahul@gmail.com")).thenReturn(true);


        assertThrows(UserEmailNotFoundException.class, () -> authService.signUp(request));


        verify(userRepository).existsByUsername("Rahul");

        verify(userRepository).existsByEmail("rahul@gmail.com");

        verify(userRepository, never()).save(any(User.class));
    }


    // ---------------------------------------------------------
    // LOGIN - SUCCESS
    // ---------------------------------------------------------

    @Test
    void loginUser_ShouldReturnTokensSuccessfully() {

        LoginRequest request = new LoginRequest();

        request.setUsername("Pranay");
        request.setPassword("Pranay123");


        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);


        when(jwtUtil.generateAccessToken("Pranay")).thenReturn("access-token");

        when(jwtUtil.generateRefreshToken("Pranay")).thenReturn("refresh-token");


        AuthResponse response = authService.loginUser(request);


        assertNotNull(response);

        assertEquals("access-token", response.getAccessToken());

        assertEquals("refresh-token", response.getRefreshToken());


        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtUtil).generateAccessToken("Pranay");

        verify(jwtUtil).generateRefreshToken("Pranay");

        verifyNoInteractions(userRepository);
    }


    // ---------------------------------------------------------
    // LOGIN - USER NOT FOUND / AUTHENTICATION FAILED
    // ---------------------------------------------------------

    @Test
    void loginUser_WhenAuthenticationFails_ShouldThrowException() {

        LoginRequest request = new LoginRequest();

        request.setUsername("Unknown");
        request.setPassword("Unknown123");


        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new UsernameNotFoundException("User not found"));


        assertThrows(UsernameNotFoundException.class, () -> authService.loginUser(request));


        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));


        verify(jwtUtil, never()).generateAccessToken(any(String.class));

        verify(jwtUtil, never()).generateRefreshToken(any(String.class));

        verifyNoInteractions(userRepository);
    }
}