package com.resource_booking_system.Controller;

import com.resource_booking_system.Configure.JwtUtil;
import com.resource_booking_system.Configure.MyUserDetailsService;
import com.resource_booking_system.Dto.ResourceDto;
import com.resource_booking_system.IService.IResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"app.pagination.max-page-size=50"})
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IResourceService resourceService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private MyUserDetailsService myUserDetailsService;


    @Test
    void createResource_ShouldReturnCreated() throws Exception {

        ResourceDto request = new ResourceDto();

        request.setName("Conference Room");
        request.setDescription("Meeting room");
        request.setType("room");
        request.setAvailable(true);


        ResourceDto response = new ResourceDto();

        response.setId(1L);
        response.setName("Conference Room");
        response.setDescription("Meeting room");
        response.setType("room");
        response.setAvailable(true);


        when(resourceService.createResource(any(ResourceDto.class))).thenReturn(response);


        mockMvc.perform(post("/api/resources").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Conference Room")).andExpect(jsonPath("$.description").value("Meeting room")).andExpect(jsonPath("$.type").value("room")).andExpect(jsonPath("$.available").value(true));


        verify(resourceService).createResource(any(ResourceDto.class));
    }


    @Test
    void getResourceById_ShouldReturnSuccess() throws Exception {

        ResourceDto response = new ResourceDto();

        response.setId(1L);
        response.setName("Conference Room");
        response.setDescription("Meeting room");
        response.setType("room");
        response.setAvailable(true);


        when(resourceService.getResourceById(1L)).thenReturn(response);


        mockMvc.perform(get("/api/resources/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Conference Room")).andExpect(jsonPath("$.type").value("room")).andExpect(jsonPath("$.available").value(true));


        verify(resourceService).getResourceById(1L);
    }


    @Test
    void getAllResource_ShouldReturnSuccess() throws Exception {

        ResourceDto resource1 = new ResourceDto();

        resource1.setId(1L);
        resource1.setName("Conference Room");
        resource1.setDescription("Meeting room");
        resource1.setType("room");
        resource1.setAvailable(true);


        ResourceDto resource2 = new ResourceDto();

        resource2.setId(2L);
        resource2.setName("Projector");
        resource2.setDescription("Office projector");
        resource2.setType("equipment");
        resource2.setAvailable(true);


        PageImpl<ResourceDto> page = new PageImpl<>(List.of(resource1, resource2), PageRequest.of(0, 10), 2);


        when(resourceService.getAllResource(any())).thenReturn(page);


        mockMvc.perform(get("/api/resources").param("page", "0").param("size", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(2)).andExpect(jsonPath("$.content[0].id").value(1)).andExpect(jsonPath("$.content[0].name").value("Conference Room")).andExpect(jsonPath("$.content[1].id").value(2)).andExpect(jsonPath("$.content[1].name").value("Projector"));


        verify(resourceService).getAllResource(any());
    }


    @Test
    void updateResource_ShouldReturnSuccess() throws Exception {

        ResourceDto request = new ResourceDto();

        request.setName("Updated Room");
        request.setDescription("Updated meeting room");
        request.setType("room");
        request.setAvailable(true);


        ResourceDto response = new ResourceDto();

        response.setId(1L);
        response.setName("Updated Room");
        response.setDescription("Updated meeting room");
        response.setType("room");
        response.setAvailable(true);


        when(resourceService.updateResource(eq(1L), any(ResourceDto.class))).thenReturn(response);


        mockMvc.perform(put("/api/resources/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Updated Room")).andExpect(jsonPath("$.description").value("Updated meeting room")).andExpect(jsonPath("$.type").value("room")).andExpect(jsonPath("$.available").value(true));


        verify(resourceService).updateResource(eq(1L), any(ResourceDto.class));
    }


    @Test
    void deleteResource_ShouldReturnNoContent() throws Exception {

        doNothing().when(resourceService).deleteResource(1L);

        mockMvc.perform(delete("/api/resources/1")).andExpect(status().isNoContent());

        verify(resourceService).deleteResource(1L);
    }

    @Test
    void getAllResource_invalidSortField_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/resources").param("sort", "user.password,asc")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid sort field: user.password"));

        verify(resourceService, never()).getAllResource(any());
    }


    @Test
    void getAllResource_pageSizeExceedsMax_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/resources").param("size", "999")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Page size must not exceed 50"));

        verify(resourceService, never()).getAllResource(any());
    }

    @Test
    void getResourceById_negativeId_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/resources/-1")).andExpect(status().isBadRequest());

        verify(resourceService, never()).getResourceById(any());
    }

}