package com.resource_booking_system.Service;

import com.resource_booking_system.Dto.ReservationRequest;
import com.resource_booking_system.Dto.ReservationResponse;
import com.resource_booking_system.Dto.ReservationUpdateRequest;
import com.resource_booking_system.Entity.Reservation;
import com.resource_booking_system.Entity.ReservationStatus;
import com.resource_booking_system.Entity.Resource;
import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Exception.*;
import com.resource_booking_system.Repository.ReservationRepository;
import com.resource_booking_system.Repository.ResourceRepository;
import com.resource_booking_system.Repository.UserRepository;
import com.resource_booking_system.ServiceImpl.ReservationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;


    @Test
    void createReservation_ShouldCreateSuccessfully() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500.00"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");
        user.setEmail("ram@gmail.com");
        user.setRole(Role.USER);

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setType("ROOM");
        resource.setAvailable(true);

        Reservation savedReservation = new Reservation();

        savedReservation.setId(1L);
        savedReservation.setUser(user);
        savedReservation.setResource(resource);
        savedReservation.setPrice(new BigDecimal("500.00"));
        savedReservation.setStartTime(startTime);
        savedReservation.setEndTime(endTime);
        savedReservation.setStatus(ReservationStatus.PENDING);

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.of(user));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(reservationRepository.existsOverlappingReservation(eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationResponse response = reservationService.createReservation(request, "Ram");

        assertNotNull(response);

        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("Ram", response.getUsername());
        assertEquals(1L, response.getResourceId());
        assertEquals("Meeting Room", response.getResourceName());
        assertEquals(new BigDecimal("500.00"), response.getPrice());
        assertEquals(ReservationStatus.PENDING, response.getStatus());

        verify(userRepository).findByUsername("Ram");
        verify(resourceRepository).findById(1L);
        verify(reservationRepository).save(any(Reservation.class));
    }


    @Test
    void createReservation_WhenUserNotFound_ShouldThrowException() {

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500.00"));
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> reservationService.createReservation(request, "Ram"));

        verify(userRepository).findByUsername("Ram");
        verify(resourceRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }


    @Test
    void createReservation_WhenResourceNotFound_ShouldThrowException() {

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(99L);
        request.setPrice(new BigDecimal("500.00"));
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.of(user));

        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservationService.createReservation(request, "Ram"));

        verify(resourceRepository).findById(99L);
        verify(reservationRepository, never()).save(any());
    }


    @Test
    void createReservation_WhenResourceUnavailable_ShouldThrowException() {

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500.00"));
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setAvailable(false);

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.of(user));

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request, "Ram"));

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void createReservation_WhenStartAfterEnd_ShouldThrowException() {

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500.00"));

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        request.setStartTime(startTime);
        request.setEndTime(endTime);

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setAvailable(true);

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.of(user));

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request, "Ram"));

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void createReservation_WhenStartTimeIsPast_ShouldThrowException() {

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500.00"));

        LocalDateTime startTime = LocalDateTime.now().minusHours(2);

        LocalDateTime endTime = LocalDateTime.now().plusHours(2);

        request.setStartTime(startTime);
        request.setEndTime(endTime);

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setAvailable(true);

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.of(user));

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request, "Ram"));

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void getReservationById_ShouldReturnOwnReservation() {

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setPrice(new BigDecimal("500.00"));
        reservation.setStartTime(LocalDateTime.now().plusDays(1));
        reservation.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(1L, "Ram", false);

        assertNotNull(response);

        assertEquals(1L, response.getId());
        assertEquals("Ram", response.getUsername());
        assertEquals(ReservationStatus.PENDING, response.getStatus());

        verify(reservationRepository).findById(1L);
    }


    @Test
    void getReservationById_WhenAnotherUser_ShouldThrowException() {

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(ForbiddenException.class, () -> reservationService.getReservationById(1L, "Shyam", false));
    }


    @Test
    void getReservationById_Admin_ShouldReturnReservation() {

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setPrice(new BigDecimal("500.00"));
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(1L, "Pranay", true);

        assertNotNull(response);

        assertEquals(1L, response.getId());
        assertEquals("Ram", response.getUsername());
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }


    @Test
    void getReservationById_WhenNotFound_ShouldThrowException() {

        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> reservationService.getReservationById(99L, "Ram", false));
    }


    @Test
    void getAllReservation_User_ShouldReturnOwnReservations() {

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setPrice(new BigDecimal("500.00"));
        reservation.setStatus(ReservationStatus.PENDING);

        Page<Reservation> page = new PageImpl<>(List.of(reservation));

        Pageable pageable = PageRequest.of(0, 5);

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.of(user));

        when(reservationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ReservationResponse> response = reservationService.getAllReservation(null, null, null, "Ram", false, pageable);

        assertNotNull(response);

        assertEquals(1, response.getTotalElements());
        assertEquals("Ram", response.getContent().get(0).getUsername());

        verify(userRepository).findByUsername("Ram");

        verify(reservationRepository).findAll(any(Specification.class), eq(pageable));
    }


    @Test
    void getAllReservation_Admin_ShouldReturnAllReservations() {

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setPrice(new BigDecimal("500.00"));
        reservation.setStatus(ReservationStatus.CONFIRMED);

        Page<Reservation> page = new PageImpl<>(List.of(reservation));

        Pageable pageable = PageRequest.of(0, 10);

        when(reservationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ReservationResponse> response = reservationService.getAllReservation(null, null, null, "Pranay", true, pageable);

        assertNotNull(response);

        assertEquals(1, response.getTotalElements());

        assertEquals("Ram", response.getContent().get(0).getUsername());

        verify(userRepository, never()).findByUsername(anyString());

        verify(reservationRepository).findAll(any(Specification.class), eq(pageable));
    }


    @Test
    void getAllReservation_WhenMinPriceGreaterThanMaxPrice_ShouldThrowException() {

        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(BadRequestException.class, () -> reservationService.getAllReservation(null, new BigDecimal("1000"), new BigDecimal("500"), "Ram", false, pageable));

        verify(reservationRepository, never()).findAll(any(Specification.class), eq(pageable));
    }


    @Test
    void updateReservation_Admin_ShouldUpdateSuccessfully() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        LocalDateTime endTime = startTime.plusHours(3);

        User user = new User();

        user.setId(1L);
        user.setUsername("Ram");

        Resource oldResource = new Resource();

        oldResource.setId(1L);
        oldResource.setName("Old Room");
        oldResource.setAvailable(true);

        Resource newResource = new Resource();

        newResource.setId(2L);
        newResource.setName("New Room");
        newResource.setAvailable(true);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(oldResource);
        reservation.setPrice(new BigDecimal("500.00"));
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setResourceId(2L);
        request.setPrice(new BigDecimal("1000.00"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(resourceRepository.findById(2L)).thenReturn(Optional.of(newResource));
        when(reservationRepository.existsOverlappingReservationExcludingId(eq(2L), eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.updateReservation(1L, request, true);

        assertNotNull(response);

        assertEquals(2L, response.getResourceId());
        assertEquals(new BigDecimal("1000.00"), response.getPrice());
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());

        verify(reservationRepository).findById(1L);
        verify(resourceRepository).findById(2L);
        verify(reservationRepository).save(reservation);
    }


    @Test
    void updateReservation_WhenUser_ShouldThrowException() {

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        assertThrows(ForbiddenException.class, () -> reservationService.updateReservation(1L, request, false));

        verify(reservationRepository, never()).findById(anyLong());

        verify(resourceRepository, never()).findById(any());

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_WhenNotFound_ShouldThrowException() {

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> reservationService.updateReservation(99L, request, true));

        verify(reservationRepository).findById(99L);
    }


    @Test
    void updateReservation_WhenStartAfterEnd_ShouldThrowException() {

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setStartTime(LocalDateTime.now().plusDays(2));

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setStartTime(LocalDateTime.now().plusDays(2));

        request.setEndTime(LocalDateTime.now().plusDays(1));

        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(resourceRepository, never()).findById(any());

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_WhenResourceNotFound_ShouldThrowException() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        LocalDateTime endTime = startTime.plusHours(2);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setResourceId(99L);
        request.setPrice(new BigDecimal("500"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_PendingToConfirmed_ShouldWork() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        LocalDateTime endTime = startTime.plusHours(2);

        User user = new User();
        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();
        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setAvailable(true);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(ReservationStatus.PENDING);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(reservationRepository.existsOverlappingReservationExcludingId(eq(1L), eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.updateReservation(1L, request, true);

        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }


    @Test
    void updateReservation_PendingToCancelled_ShouldWork() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        LocalDateTime endTime = startTime.plusHours(2);

        User user = new User();
        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setAvailable(true);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(ReservationStatus.PENDING);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(reservationRepository.existsOverlappingReservationExcludingId(eq(1L), eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.updateReservation(1L, request, true);

        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
    }


    @Test
    void updateReservation_ConfirmedToCancelled_ShouldWork() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        LocalDateTime endTime = startTime.plusHours(2);

        User user = new User();
        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setAvailable(true);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(reservationRepository.existsOverlappingReservationExcludingId(eq(1L), eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.updateReservation(1L, request, true);

        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
    }


    @Test
    void updateReservation_CancelledReservation_ShouldThrowException() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setStartTime(startTime);
        reservation.setStatus(ReservationStatus.CANCELLED);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setResourceId(1L);
        request.setStartTime(startTime);
        request.setEndTime(startTime.plusHours(2));
        request.setPrice(new BigDecimal("500"));
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(resourceRepository, never()).findById(any());

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_InvalidStatusTransition_ShouldThrowException() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setStartTime(startTime);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setStartTime(startTime);
        request.setEndTime(startTime.plusHours(2));
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(resourceRepository, never()).findById(any());

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_StatusChangeAfterStart_ShouldThrowException() {

        LocalDateTime pastStart = LocalDateTime.now().minusHours(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusHours(1);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartTime(pastStart);
        reservation.setEndTime(futureEnd);
        reservation.setStatus(ReservationStatus.PENDING);

        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStartTime(LocalDateTime.now().minusMinutes(30));
        request.setEndTime(LocalDateTime.now().plusHours(1));
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(resourceRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_StatusChangeAfterEnd_ShouldThrowException() {

        LocalDateTime pastStart = LocalDateTime.now().minusHours(3);
        LocalDateTime pastEnd = LocalDateTime.now().minusHours(1);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartTime(pastStart);
        reservation.setEndTime(pastEnd);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStartTime(LocalDateTime.now().minusMinutes(30));
        request.setEndTime(LocalDateTime.now().plusHours(1));
        request.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(resourceRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_NullStatus_ShouldThrowException() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setStartTime(startTime);
        reservation.setStatus(ReservationStatus.PENDING);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setStartTime(startTime);
        request.setEndTime(startTime.plusHours(2));
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStatus(null);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(resourceRepository, never()).findById(any());

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_NullCurrentStatus_ShouldThrowException() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);

        Reservation reservation = new Reservation();

        reservation.setId(1L);
        reservation.setStartTime(startTime);
        reservation.setStatus(null);

        ReservationUpdateRequest request = new ReservationUpdateRequest();

        request.setStartTime(startTime);
        request.setEndTime(startTime.plusHours(2));
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(resourceRepository, never()).findById(any());

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void deleteReservation_Admin_ShouldDeleteSuccessfully() {

        Reservation reservation = new Reservation();

        reservation.setId(1L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(1L, true);

        verify(reservationRepository).findById(1L);

        verify(reservationRepository).delete(reservation);
    }


    @Test
    void deleteReservation_WhenUser_ShouldThrowException() {

        assertThrows(ForbiddenException.class, () -> reservationService.deleteReservation(1L, false));

        verify(reservationRepository, never()).findById(anyLong());

        verify(reservationRepository, never()).delete(any(Reservation.class));
    }


    @Test
    void deleteReservation_WhenNotFound_ShouldThrowException() {

        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> reservationService.deleteReservation(99L, true));

        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    @Test
    void createReservation_WhenCancelledReservationExists_ShouldAllowNewBooking() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);

        ReservationRequest request = new ReservationRequest();
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500.00"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        User user = new User();
        user.setId(1L);
        user.setUsername("Ram");
        user.setRole(Role.USER);

        Resource resource = new Resource();
        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setAvailable(true);

        Reservation savedReservation = new Reservation();
        savedReservation.setId(2L);
        savedReservation.setUser(user);
        savedReservation.setResource(resource);
        savedReservation.setPrice(new BigDecimal("500.00"));
        savedReservation.setStartTime(startTime);
        savedReservation.setEndTime(endTime);
        savedReservation.setStatus(ReservationStatus.PENDING);

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.of(user));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        when(reservationRepository.existsOverlappingReservation(eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationResponse response = reservationService.createReservation(request, "Ram");

        assertNotNull(response);
        assertEquals(ReservationStatus.PENDING, response.getStatus());

        verify(reservationRepository).existsOverlappingReservation(1L, ReservationStatus.CANCELLED, startTime, endTime);
        verify(reservationRepository).save(any(Reservation.class));
    }


    @Test
    void createReservation_WhenActiveReservationExists_ShouldThrowException() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);

        ReservationRequest request = new ReservationRequest();
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500.00"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        User user = new User();
        user.setId(1L);
        user.setUsername("Ram");
        user.setRole(Role.USER);

        Resource resource = new Resource();
        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setAvailable(true);

        when(userRepository.findByUsername("Ram")).thenReturn(Optional.of(user));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        when(reservationRepository.existsOverlappingReservation(eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(true);

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request, "Ram"));

        verify(reservationRepository).existsOverlappingReservation(1L, ReservationStatus.CANCELLED, startTime, endTime);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    @Test
    void updateReservation_WhenCancelledReservationOverlaps_ShouldAllowUpdate() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime = startTime.plusHours(2);

        User user = new User();
        user.setId(1L);
        user.setUsername("Ram");

        Resource oldResource = new Resource();
        oldResource.setId(1L);
        oldResource.setAvailable(true);

        Resource newResource = new Resource();
        newResource.setId(2L);
        newResource.setName("New Room");
        newResource.setAvailable(true);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(oldResource);
        reservation.setPrice(new BigDecimal("500.00"));
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setResourceId(2L);
        request.setPrice(new BigDecimal("1000.00"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(resourceRepository.findById(2L)).thenReturn(Optional.of(newResource));

        when(reservationRepository.existsOverlappingReservationExcludingId(eq(2L), eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.updateReservation(1L, request, true);

        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());

        verify(reservationRepository).existsOverlappingReservationExcludingId(2L, 1L, ReservationStatus.CANCELLED, startTime, endTime);
    }


    @Test
    void updateReservation_ConfirmedWithPastStartTime_ShouldThrowException() {

        LocalDateTime futureOriginalStart = LocalDateTime.now().plusDays(2);
        LocalDateTime pastStart = LocalDateTime.now().minusHours(1);

        User user = new User();
        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();
        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setAvailable(true);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(futureOriginalStart);
        reservation.setEndTime(futureOriginalStart.plusHours(2));
        reservation.setStatus(ReservationStatus.PENDING);

        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStartTime(pastStart);
        request.setEndTime(pastStart.plusHours(2));
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> reservationService.updateReservation(1L, request, true));

        verify(resourceRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_ConfirmedWithFutureStartTime_ShouldSucceed() {

        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime = startTime.plusHours(2);

        User user = new User();
        user.setId(1L);
        user.setUsername("Ram");

        Resource resource = new Resource();
        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setAvailable(true);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(ReservationStatus.PENDING);

        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setResourceId(1L);
        request.setPrice(new BigDecimal("500"));
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(reservationRepository.existsOverlappingReservationExcludingId(eq(1L), eq(1L), eq(ReservationStatus.CANCELLED), eq(startTime), eq(endTime))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.updateReservation(1L, request, true);

        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }

}