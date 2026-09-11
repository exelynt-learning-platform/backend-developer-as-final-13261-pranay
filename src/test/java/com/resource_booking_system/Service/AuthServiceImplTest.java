package com.resource_booking_system.Service;

import com.resource_booking_system.Configure.JwtUtil;
import com.resource_booking_system.Dto.AuthResponse;
import com.resource_booking_system.Dto.LoginRequest;
import com.resource_booking_system.Dto.RefreshTokenRequest;
import com.resource_booking_system.Dto.SignUpRequest;
import com.resource_booking_system.Dto.SignUpResponse;
import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Exception.UserEmailNotFoundException;
import com.resource_booking_system.Repository.UserRepository;
import com.resource_booking_system.ServiceImpl.AuthServiceImpl;

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



    @Test
    void refreshAccessToken_ShouldReturnNewAccessTokenSuccessfully() {

        RefreshTokenRequest request = new RefreshTokenRequest();

        request.setRefreshToken("refresh-token");

        when(jwtUtil.validateRefreshToken("refresh-token")).thenReturn(true);

        when(jwtUtil.extractUsername("refresh-token")).thenReturn("Pranay");

        when(jwtUtil.generateAccessToken("Pranay")).thenReturn("new-access-token");

        AuthResponse response = authService.refreshAccessToken(request);

        assertNotNull(response);

        assertEquals("new-access-token", response.getAccessToken());

        assertEquals("refresh-token", response.getRefreshToken());

        verify(jwtUtil).validateRefreshToken("refresh-token");

        verify(jwtUtil).extractUsername("refresh-token");

        verify(jwtUtil).generateAccessToken("Pranay");

        verifyNoInteractions(userRepository);
    }


    @Test
    void refreshAccessToken_WhenTokenInvalid_ShouldThrowException() {

        RefreshTokenRequest request = new RefreshTokenRequest();

        request.setRefreshToken("invalid-token");

        when(jwtUtil.validateRefreshToken("invalid-token")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.refreshAccessToken(request));

        verify(jwtUtil).validateRefreshToken("invalid-token");

        verify(jwtUtil, never()).extractUsername(any(String.class));

        verify(jwtUtil, never()).generateAccessToken(any(String.class));
    }



    @Test
    void refreshAccessToken_WhenTokenExpired_ShouldThrowException() {

        RefreshTokenRequest request = new RefreshTokenRequest();

        request.setRefreshToken("expired-token");

        when(jwtUtil.validateRefreshToken("expired-token")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.refreshAccessToken(request));

        verify(jwtUtil).validateRefreshToken("expired-token");

        verify(jwtUtil, never()).extractUsername(any(String.class));

        verify(jwtUtil, never()).generateAccessToken(any(String.class));
    }
}