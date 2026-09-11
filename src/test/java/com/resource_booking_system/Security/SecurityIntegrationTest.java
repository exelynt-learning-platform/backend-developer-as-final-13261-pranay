package com.resource_booking_system.Security;

import com.resource_booking_system.Configure.JwtUtil;
import com.resource_booking_system.Configure.MyUserDetailsService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private MyUserDetailsService myUserDetailsService;


    @Test
    void unauthenticatedUser_ShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/resources")).andExpect(status().isUnauthorized());
    }


    @Test
    void user_ShouldNotAccessAdminResourceEndpoint() throws Exception {

        UserDetails userDetails = User.withUsername("Ram").password("password").authorities("USER").build();

        when(myUserDetailsService.loadUserByUsername("Ram")).thenReturn(userDetails);

        String token = jwtUtil.generateAccessToken(userDetails);

        mockMvc.perform(post("/api/resources").header("Authorization", "Bearer " + token).contentType("application/json").content("""
                {
                    "name": "Conference Room",
                    "description": "Meeting Room",
                    "type": "room",
                    "available": true
                }
                """)).andExpect(status().isForbidden());
    }


    @Test
    void adminUser_ShouldAccessAdminProtectedEndpoint() throws Exception {

        UserDetails adminDetails = User.withUsername("Pranay").password("password").authorities("ADMIN").build();

        when(myUserDetailsService.loadUserByUsername("Pranay")).thenReturn(adminDetails);

        String token = jwtUtil.generateAccessToken(adminDetails);

        mockMvc.perform(get("/api/resources").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }
}