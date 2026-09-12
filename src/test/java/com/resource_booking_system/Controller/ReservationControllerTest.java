package com.resource_booking_system.Controller;

import com.resource_booking_system.Configure.JwtUtil;
import com.resource_booking_system.Configure.MyUserDetailsService;
import com.resource_booking_system.Dto.ReservationRequest;
import com.resource_booking_system.Dto.ReservationResponse;
import com.resource_booking_system.Dto.ReservationStatusUpdateRequest;
import com.resource_booking_system.Dto.ReservationUpdateRequest;
import com.resource_booking_system.Entity.ReservationStatus;
import com.resource_booking_system.IService.IReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.resource_booking_system.Configure.PageableValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.annotation.Import;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"app.pagination.max-page-size=50"})
@Import(PageableValidator.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReservationService reservationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private MyUserDetailsService myUserDetailsService;

    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Test
    @WithMockUser(username = "john", authorities = "USER")
    void createReservation_success() throws Exception {

        ReservationRequest request = new ReservationRequest();
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500.00"));
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setResourceId(1L);
        response.setResourceName("Conference Room");
        response.setUsername("john");
        response.setPrice(new BigDecimal("500.00"));
        response.setStartTime(request.getStartTime());
        response.setEndTime(request.getEndTime());
        response.setStatus(ReservationStatus.PENDING);

        when(reservationService.createReservation(any(ReservationRequest.class), eq("john"))).thenReturn(response);

        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.username").value("john")).andExpect(jsonPath("$.resourceName").value("Conference Room")).andExpect(jsonPath("$.status").value("PENDING"));

        verify(reservationService).createReservation(any(ReservationRequest.class), eq("john"));
    }


    @Test
    @WithMockUser(username = "john", authorities = "USER")
    void getReservationById_user_success() throws Exception {

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUsername("john");
        response.setResourceId(1L);
        response.setResourceName("Conference Room");
        response.setPrice(new BigDecimal("500.00"));
        response.setStatus(ReservationStatus.PENDING);

        when(reservationService.getReservationById(eq(1L), eq("john"), eq(false))).thenReturn(response);

        mockMvc.perform(get("/api/reservations/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.username").value("john")).andExpect(jsonPath("$.status").value("PENDING"));

        verify(reservationService).getReservationById(1L, "john", false);
    }


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void getReservationById_admin_success() throws Exception {

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUsername("john");
        response.setResourceId(1L);
        response.setResourceName("Conference Room");
        response.setPrice(new BigDecimal("500.00"));
        response.setStatus(ReservationStatus.CONFIRMED);

        when(reservationService.getReservationById(eq(1L), eq("admin"), eq(true))).thenReturn(response);

        mockMvc.perform(get("/api/reservations/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.username").value("john")).andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService).getReservationById(1L, "admin", true);
    }


    @Test
    @WithMockUser(username = "john", authorities = "USER")
    void getAllReservation_user_success() throws Exception {

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUsername("john");
        response.setResourceName("Conference Room");
        response.setPrice(new BigDecimal("500.00"));
        response.setStatus(ReservationStatus.PENDING);

        PageImpl<ReservationResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(reservationService.getAllReservation(isNull(), isNull(), isNull(), eq("john"), eq(false), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/reservations").param("page", "0").param("size", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1)).andExpect(jsonPath("$.content[0].username").value("john")).andExpect(jsonPath("$.content[0].status").value("PENDING"));

        verify(reservationService).getAllReservation(isNull(), isNull(), isNull(), eq("john"), eq(false), any(PageRequest.class));
    }


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void getAllReservation_admin_success() throws Exception {

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUsername("john");
        response.setResourceName("Conference Room");
        response.setPrice(new BigDecimal("500.00"));
        response.setStatus(ReservationStatus.CONFIRMED);

        PageImpl<ReservationResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(reservationService.getAllReservation(isNull(), isNull(), isNull(), eq("admin"), eq(true), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/reservations").param("page", "0").param("size", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1)).andExpect(jsonPath("$.content[0].username").value("john")).andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));

        verify(reservationService).getAllReservation(isNull(), isNull(), isNull(), eq("admin"), eq(true), any(PageRequest.class));
    }


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void getAllReservation_withFilter_success() throws Exception {

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUsername("john");
        response.setResourceName("Conference Room");
        response.setPrice(new BigDecimal("500.00"));
        response.setStatus(ReservationStatus.CONFIRMED);

        PageImpl<ReservationResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(reservationService.getAllReservation(eq(ReservationStatus.CONFIRMED), eq(new BigDecimal("100.00")), eq(new BigDecimal("1000.00")), eq("admin"), eq(true), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/reservations").param("status", "CONFIRMED").param("minPrice", "100.00").param("maxPrice", "1000.00").param("page", "0").param("size", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].status").value("CONFIRMED")).andExpect(jsonPath("$.content[0].price").value(500.00));

        verify(reservationService).getAllReservation(eq(ReservationStatus.CONFIRMED), eq(new BigDecimal("100.00")), eq(new BigDecimal("1000.00")), eq("admin"), eq(true), any(PageRequest.class));
    }


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void getAllReservation_invalidSortField_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/reservations").param("sort", "user.password,asc").param("page", "0").param("size", "10")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.error").value("Bad Request")).andExpect(jsonPath("$.message").value("Invalid sort field: user.password"));

        verify(reservationService, never()).getAllReservation(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class));
    }


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void getAllReservation_pageSizeExceedsMax_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/reservations").param("page", "0").param("size", "999")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.message").value("Page size must not exceed 50"));

        verify(reservationService, never()).getAllReservation(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class));
    }


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void updateReservation_success() throws Exception {

        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("700.00"));
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        request.setStatus(ReservationStatus.CONFIRMED);

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUsername("john");
        response.setResourceId(1L);
        response.setPrice(new BigDecimal("700.00"));
        response.setStatus(ReservationStatus.CONFIRMED);

        when(reservationService.updateReservation(eq(1L), any(ReservationUpdateRequest.class), eq(true))).thenReturn(response);

        mockMvc.perform(put("/api/reservations/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.status").value("CONFIRMED")).andExpect(jsonPath("$.price").value(700.00));

        verify(reservationService).updateReservation(eq(1L), any(ReservationUpdateRequest.class), eq(true));
    }


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void patchStatus_shouldReturnUpdatedReservation() throws Exception {

        ReservationStatusUpdateRequest request = new ReservationStatusUpdateRequest();
        request.setStatus(ReservationStatus.CONFIRMED);

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setUsername("john");
        response.setStatus(ReservationStatus.CONFIRMED);

        when(reservationService.updateReservationStatus(eq(1L), any(ReservationStatusUpdateRequest.class), eq(true))).thenReturn(response);

        mockMvc.perform(patch("/api/reservations/1/status").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService).updateReservationStatus(eq(1L), any(ReservationStatusUpdateRequest.class), eq(true));
    }


    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void deleteReservation_success() throws Exception {

        doNothing().when(reservationService).deleteReservation(1L, true);

        mockMvc.perform(delete("/api/reservations/1")).andExpect(status().isNoContent());

        verify(reservationService).deleteReservation(1L, true);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void getReservationById_negativeId_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/reservations/-1")).andExpect(status().isBadRequest());

        verify(reservationService, never()).getReservationById(any(), any(), anyBoolean());
    }
}