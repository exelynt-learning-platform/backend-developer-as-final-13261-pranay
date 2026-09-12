package com.resource_booking_system.Integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.resource_booking_system.Dto.LoginRequest;
import com.resource_booking_system.Dto.ReservationRequest;
import com.resource_booking_system.Dto.ResourceDto;
import com.resource_booking_system.Dto.SignUpRequest;
import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Repository.ReservationRepository;
import com.resource_booking_system.Repository.ResourceRepository;
import com.resource_booking_system.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationE2EIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long resourceId;

    @BeforeEach
    void setUp() {

        reservationRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        var resource = new com.resource_booking_system.Entity.Resource();
        resource.setName("Conference Room A");
        resource.setDescription("Big room");
        resource.setType("ROOM");
        resource.setAvailable(true);
        resourceId = resourceRepository.save(resource).getId();
    }


    private String signUpAndLogin(String username, String email, String password) throws Exception {

        SignUpRequest signUp = new SignUpRequest();
        signUp.setUsername(username);
        signUp.setEmail(email);
        signUp.setPassword(password);

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(signUp))).andExpect(status().isCreated()).andExpect(jsonPath("$.username").value(username));

        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login))).andExpect(status().isOk()).andReturn();

        JsonNode node = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }

    @Test
    void e2e_signupLoginAndAccessProtectedEndpoint_success() throws Exception {

        String token = signUpAndLogin("alice", "alice@test.com", "Alice@123");

        mockMvc.perform(get("/api/resources").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }


    @Test
    void e2e_userForbiddenOnAdminEndpoint() throws Exception {

        String token = signUpAndLogin("bob", "bob@test.com", "Bob@123");

        ResourceDto dto = new ResourceDto();
        dto.setName("New Room");
        dto.setDescription("Desc");
        dto.setType("ROOM");
        dto.setAvailable(true);

        mockMvc.perform(post("/api/resources").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isForbidden());
    }


    @Test
    void e2e_userSeesOnlyOwnReservations() throws Exception {

        String aliceToken = signUpAndLogin("alice2", "alice2@test.com", "Alice@123");
        String bobToken = signUpAndLogin("bob2", "bob2@test.com", "Bob@123");

        ReservationRequest req = new ReservationRequest();
        req.setResourceId(resourceId);
        req.setPrice(new BigDecimal("100.00"));
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        mockMvc.perform(post("/api/reservations").header("Authorization", "Bearer " + aliceToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/reservations").header("Authorization", "Bearer " + bobToken)).andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get("/api/reservations").header("Authorization", "Bearer " + aliceToken)).andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));
    }


    @Test
    void e2e_userCannotAccessOthersReservationById() throws Exception {

        String aliceToken = signUpAndLogin("alice3", "alice3@test.com", "Alice@123");
        String bobToken = signUpAndLogin("bob3", "bob3@test.com", "Bob@123");

        ReservationRequest req = new ReservationRequest();
        req.setResourceId(resourceId);
        req.setPrice(new BigDecimal("100.00"));
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        MvcResult result = mockMvc.perform(post("/api/reservations").header("Authorization", "Bearer " + aliceToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))).andExpect(status().isCreated()).andReturn();

        Long reservationId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/reservations/" + reservationId).header("Authorization", "Bearer " + aliceToken)).andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/" + reservationId).header("Authorization", "Bearer " + bobToken)).andExpect(status().isForbidden());
    }


    @Test
    void e2e_adminCanAccessAnyReservation() throws Exception {

        String aliceToken = signUpAndLogin("alice4", "alice4@test.com", "Alice@123");

        ReservationRequest req = new ReservationRequest();
        req.setResourceId(resourceId);
        req.setPrice(new BigDecimal("100.00"));
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        MvcResult result = mockMvc.perform(post("/api/reservations").header("Authorization", "Bearer " + aliceToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req))).andExpect(status().isCreated()).andReturn();

        Long reservationId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("admin");
        adminLogin.setPassword("Admin@123");

        MvcResult adminResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(adminLogin))).andExpect(status().isOk()).andReturn();

        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/reservations/" + reservationId).header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("alice4"));
    }


    @Test
    void e2e_noTokenReturnsUnauthorized() throws Exception {

        mockMvc.perform(get("/api/reservations")).andExpect(status().isUnauthorized());
    }


    @Test
    void e2e_invalidTokenReturnsUnauthorized() throws Exception {

        mockMvc.perform(get("/api/reservations").header("Authorization", "Bearer not.a.real.token")).andExpect(status().isUnauthorized());
    }


    @Test
    void e2e_refreshTokenProducesWorkingAccessToken() throws Exception {

        SignUpRequest signUp = new SignUpRequest();
        signUp.setUsername("carol");
        signUp.setEmail("carol@test.com");
        signUp.setPassword("Carol@123");

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(signUp))).andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername("carol");
        login.setPassword("Carol@123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login))).andExpect(status().isOk()).andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + refreshToken + "\"}")).andExpect(status().isOk()).andReturn();

        String newAccessToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/reservations").header("Authorization", "Bearer " + newAccessToken)).andExpect(status().isOk());
    }


    @Test
    void e2e_loginWithWrongPasswordReturnsUnauthorized() throws Exception {

        signUpAndLogin("dave", "dave@test.com", "Dave@123");

        LoginRequest wrongLogin = new LoginRequest();
        wrongLogin.setUsername("dave");
        wrongLogin.setPassword("wrong-password");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(wrongLogin))).andExpect(status().isUnauthorized());
    }


    @Test
    void e2e_duplicateSignupReturnsConflict() throws Exception {

        signUpAndLogin("eve", "eve@test.com", "Eve@123");

        SignUpRequest duplicate = new SignUpRequest();
        duplicate.setUsername("eve");
        duplicate.setEmail("newemail@test.com");
        duplicate.setPassword("Eve@123");

        mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(duplicate))).andExpect(status().isConflict());
    }
}