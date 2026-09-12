package com.resource_booking_system.Service;

import com.resource_booking_system.Configure.JwtUtil;
import com.resource_booking_system.Dto.AuthResponse;
import com.resource_booking_system.Dto.LoginRequest;
import com.resource_booking_system.Dto.RefreshTokenRequest;
import com.resource_booking_system.Dto.SignUpRequest;
import com.resource_booking_system.Dto.SignUpResponse;
import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Exception.DuplicateEmailException;
import com.resource_booking_system.Exception.DuplicateUsernameException;
import com.resource_booking_system.Exception.InvalidCredentialsException;
import com.resource_booking_system.Repository.UserRepository;
import com.resource_booking_system.ServiceImpl.AuthServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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
        savedUser.setEnabled(true);

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
    void signUp_ShouldTrimAndLowercaseEmail() {

        SignUpRequest request = new SignUpRequest();
        request.setUsername("  Rahul  ");
        request.setEmail("  RAHUL@Gmail.com  ");
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
        savedUser.setEnabled(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        SignUpResponse response = authService.signUp(request);

        assertEquals("Rahul", response.getUsername());
        assertEquals("rahul@gmail.com", response.getEmail());

        verify(userRepository).existsByUsername("Rahul");
        verify(userRepository).existsByEmail("rahul@gmail.com");
    }


    @Test
    void signUp_WhenUsernameExists_ShouldThrowDuplicateUsernameException() {

        SignUpRequest request = new SignUpRequest();
        request.setUsername("Rahul");
        request.setEmail("rahul@gmail.com");
        request.setPassword("Rahul123");

        when(userRepository.existsByUsername("Rahul")).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> authService.signUp(request));

        verify(userRepository).existsByUsername("Rahul");
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void signUp_WhenEmailExists_ShouldThrowDuplicateEmailException() {

        SignUpRequest request = new SignUpRequest();
        request.setUsername("Rahul");
        request.setEmail("rahul@gmail.com");
        request.setPassword("Rahul123");

        when(userRepository.existsByUsername("Rahul")).thenReturn(false);
        when(userRepository.existsByEmail("rahul@gmail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.signUp(request));

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
    }


    @Test
    void loginUser_WhenBadCredentials_ShouldThrowInvalidCredentialsException() {

        LoginRequest request = new LoginRequest();
        request.setUsername("Ram");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(request));

        verify(jwtUtil, never()).generateAccessToken(any(String.class));
        verify(jwtUtil, never()).generateRefreshToken(any(String.class));
    }


    @Test
    void loginUser_WhenAccountDisabled_ShouldThrowInvalidCredentialsException() {

        LoginRequest request = new LoginRequest();
        request.setUsername("Disabled");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new DisabledException("Account disabled"));

        assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(request));

        verify(jwtUtil, never()).generateAccessToken(any(String.class));
        verify(jwtUtil, never()).generateRefreshToken(any(String.class));
    }


    @Test
    void refreshAccessToken_ShouldReturnNewTokensSuccessfully() {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        Claims claims = Jwts.claims().subject("Pranay").add("type", "REFRESH").build();

        when(jwtUtil.extractAllClaims("refresh-token")).thenReturn(claims);
        when(jwtUtil.validateRefreshToken(claims)).thenReturn(true);
        when(jwtUtil.extractUsername(claims)).thenReturn("Pranay");

        User user = new User();
        user.setId(1L);
        user.setUsername("Pranay");
        user.setEmail("pranay@gmail.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user.setEnabled(true);

        when(userRepository.findByUsername("Pranay")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken("Pranay")).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken("Pranay")).thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshAccessToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());

        verify(jwtUtil).extractAllClaims("refresh-token");
        verify(jwtUtil).validateRefreshToken(claims);
        verify(userRepository).findByUsername("Pranay");
        verify(jwtUtil).generateAccessToken("Pranay");
        verify(jwtUtil).generateRefreshToken("Pranay");
    }


    @Test
    void refreshAccessToken_WhenUserNotFound_ShouldThrowInvalidCredentialsException() {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        Claims claims = Jwts.claims().subject("DeletedUser").add("type", "REFRESH").build();

        when(jwtUtil.extractAllClaims("refresh-token")).thenReturn(claims);
        when(jwtUtil.validateRefreshToken(claims)).thenReturn(true);
        when(jwtUtil.extractUsername(claims)).thenReturn("DeletedUser");
        when(userRepository.findByUsername("DeletedUser")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.refreshAccessToken(request));

        verify(jwtUtil, never()).generateAccessToken(any(String.class));
        verify(jwtUtil, never()).generateRefreshToken(any(String.class));
    }


    @Test
    void refreshAccessToken_WhenUserDisabled_ShouldThrowInvalidCredentialsException() {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        Claims claims = Jwts.claims().subject("DisabledUser").add("type", "REFRESH").build();

        when(jwtUtil.extractAllClaims("refresh-token")).thenReturn(claims);
        when(jwtUtil.validateRefreshToken(claims)).thenReturn(true);
        when(jwtUtil.extractUsername(claims)).thenReturn("DisabledUser");

        User user = new User();
        user.setId(1L);
        user.setUsername("DisabledUser");
        user.setEmail("disabled@gmail.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user.setEnabled(false);

        when(userRepository.findByUsername("DisabledUser")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> authService.refreshAccessToken(request));

        verify(jwtUtil, never()).generateAccessToken(any(String.class));
        verify(jwtUtil, never()).generateRefreshToken(any(String.class));
    }


    @Test
    void refreshAccessToken_WhenTokenInvalid_ShouldThrowInvalidCredentialsException() {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(jwtUtil.extractAllClaims("invalid-token")).thenThrow(new MalformedJwtException("Invalid token"));

        assertThrows(InvalidCredentialsException.class, () -> authService.refreshAccessToken(request));

        verify(jwtUtil, never()).validateRefreshToken(any());
        verify(userRepository, never()).findByUsername(any(String.class));
        verify(jwtUtil, never()).generateAccessToken(any(String.class));
    }


    @Test
    void refreshAccessToken_WhenTokenExpired_ShouldThrowInvalidCredentialsException() {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        when(jwtUtil.extractAllClaims("expired-token")).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        assertThrows(InvalidCredentialsException.class, () -> authService.refreshAccessToken(request));

        verify(jwtUtil, never()).validateRefreshToken(any());
        verify(userRepository, never()).findByUsername(any(String.class));
        verify(jwtUtil, never()).generateAccessToken(any(String.class));
    }
}