package com.resource_booking_system.Controller;

import com.resource_booking_system.Configure.JwtUtil;
import com.resource_booking_system.Configure.MyUserDetailsService;
import com.resource_booking_system.Dto.*;
import com.resource_booking_system.IService.IAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAuthService authService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private MyUserDetailsService myUserDetailsService;


    @Test
    void signup_ShouldReturnSuccess() throws Exception {

        SignUpRequest request = new SignUpRequest();

        request.setUsername("Ram");
        request.setEmail("ram@gmail.com");
        request.setPassword("Ram@123");


        SignUpResponse response = new SignUpResponse();

        response.setId(1L);
        response.setUsername("Ram");
        response.setEmail("ram@gmail.com");
        response.setRole("USER");


        when(authService.signUp(any(SignUpRequest.class))).thenReturn(response);


        mockMvc.perform(post("/auth/signup")

                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.username").value("Ram")).andExpect(jsonPath("$.email").value("ram@gmail.com")).andExpect(jsonPath("$.role").value("USER"));


        verify(authService).signUp(any(SignUpRequest.class));
    }


    @Test
    void login_ShouldReturnSuccess() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setUsername("Ram");
        request.setPassword("Ram@123");


        AuthResponse response = new AuthResponse();

        response.setAccessToken("access-token");
        response.setRefreshToken("refresh-token");


        when(authService.loginUser(any(LoginRequest.class))).thenReturn(response);


        mockMvc.perform(post("/auth/login")

                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("access-token")).andExpect(jsonPath("$.refreshToken").value("refresh-token"));


        verify(authService).loginUser(any(LoginRequest.class));
    }

    @Test
    void refresh_ShouldReturnNewAccessTokenSuccessfully() throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest();

        request.setRefreshToken("refresh-token");

        AuthResponse response = new AuthResponse();

        response.setAccessToken("new-access-token");
        response.setRefreshToken("refresh-token");

        when(authService.refreshAccessToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("new-access-token")).andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).refreshAccessToken(any(RefreshTokenRequest.class));
    }


}