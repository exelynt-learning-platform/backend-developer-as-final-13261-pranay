package com.resource_booking_system.ServiceImpl;

import com.resource_booking_system.Dto.ReservationRequest;
import com.resource_booking_system.Dto.ReservationResponse;
import com.resource_booking_system.Dto.ReservationUpdateRequest;
import com.resource_booking_system.Entity.Reservation;
import com.resource_booking_system.Entity.ReservationStatus;
import com.resource_booking_system.Entity.Resource;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Exception.BadRequestException;
import com.resource_booking_system.Exception.ForbiddenException;
import com.resource_booking_system.Exception.ReservationNotFoundException;
import com.resource_booking_system.Exception.ResourceNotFoundException;
import com.resource_booking_system.Exception.UsernameNotFoundException;
import com.resource_booking_system.IService.IReservationService;
import com.resource_booking_system.Repository.ReservationRepository;
import com.resource_booking_system.Repository.ResourceRepository;
import com.resource_booking_system.Repository.UserRepository;
import com.resource_booking_system.Specification.ReservationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReservationServiceImpl implements IReservationService {

    private static final String ONLY_ADMIN_UPDATE = "Only ADMIN can update reservation";

    private static final String ONLY_ADMIN_DELETE = "Only ADMIN can delete reservation";

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, UserRepository userRepository, ResourceRepository resourceRepository) {

        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public ReservationResponse createReservation(ReservationRequest reservationRequest, String username) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Resource resource = resourceRepository.findById(reservationRequest.getResourceId()).orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + reservationRequest.getResourceId()));

        validateReservationTime(reservationRequest.getStartTime(), reservationRequest.getEndTime());

        if (!resource.getAvailable()) {
            throw new BadRequestException("Resource is not available");
        }

        boolean overlap = reservationRepository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resource.getId(), reservationRequest.getEndTime(), reservationRequest.getStartTime());

        if (overlap) {
            throw new BadRequestException("Resource is already booked for the selected time");
        }

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(reservationRequest.getStartTime());
        reservation.setEndTime(reservationRequest.getEndTime());
        reservation.setPrice(reservationRequest.getPrice());
        reservation.setStatus(ReservationStatus.PENDING);

        Reservation createdReservation = reservationRepository.save(reservation);

        return mapToResponse(createdReservation);
    }

    @Override
    public ReservationResponse getReservationById(Long id, String username, boolean isAdmin) {

        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException("Reservation not found with ID: " + id));

        if (!isAdmin && !reservation.getUser().getUsername().equals(username)) {

            throw new ForbiddenException("Access denied: You can only view your own reservations");
        }

        return mapToResponse(reservation);
    }

    @Override
    public Page<ReservationResponse> getAllReservation(ReservationStatus reservationStatus, BigDecimal minPrice, BigDecimal maxPrice, String username, boolean isAdmin, Pageable pageable) {

        validatePriceRange(minPrice, maxPrice);

        Long userId = null;

        if (!isAdmin) {

            User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

            userId = user.getId();
        }

        Specification<Reservation> specification = ReservationSpecification.filterReservations(userId, reservationStatus, minPrice, maxPrice);

        Page<Reservation> reservations = reservationRepository.findAll(specification, pageable);

        return reservations.map(this::mapToResponse);
    }

    @Override
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest updateRequest, boolean isAdmin) {

        if (!isAdmin) {
            throw new ForbiddenException(ONLY_ADMIN_UPDATE);
        }

        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        validateStatusTransition(reservation.getStatus(), updateRequest.getStatus());

        validateReservationTime(updateRequest.getStartTime(), updateRequest.getEndTime());

        Resource resource = resourceRepository.findById(updateRequest.getResourceId()).orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + updateRequest.getResourceId()));

        if (!resource.getAvailable()) {
            throw new BadRequestException("Resource is not available");
        }

        boolean overlap = reservationRepository.existsByResourceIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(resource.getId(), id, updateRequest.getEndTime(), updateRequest.getStartTime());

        if (overlap) {
            throw new BadRequestException("Resource is already booked for the selected time");
        }

        reservation.setResource(resource);
        reservation.setPrice(updateRequest.getPrice());
        reservation.setStartTime(updateRequest.getStartTime());
        reservation.setEndTime(updateRequest.getEndTime());
        reservation.setStatus(updateRequest.getStatus());

        Reservation updatedReservation = reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    @Override
    public void deleteReservation(Long id, boolean isAdmin) {

        if (!isAdmin) {
            throw new ForbiddenException(ONLY_ADMIN_DELETE);
        }

        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        reservationRepository.delete(reservation);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {

        ReservationResponse response = new ReservationResponse();

        response.setId(reservation.getId());

        response.setUserId(reservation.getUser().getId());

        response.setUsername(reservation.getUser().getUsername());

        response.setResourceId(reservation.getResource().getId());

        response.setResourceName(reservation.getResource().getName());

        response.setPrice(reservation.getPrice());

        response.setStartTime(reservation.getStartTime());

        response.setEndTime(reservation.getEndTime());

        response.setStatus(reservation.getStatus());

        return response;
    }

    private void validateReservationTime(LocalDateTime startTime, LocalDateTime endTime) {

        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {

            throw new BadRequestException("Minimum price cannot be greater than maximum price");
        }
    }

    private void validateStatusTransition(ReservationStatus currentStatus, ReservationStatus newStatus) {

        if (currentStatus == null || newStatus == null) {
            throw new BadRequestException("Reservation status cannot be null");
        }

        if (currentStatus == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Cancelled reservation cannot be modified");
        }

        boolean validTransition = (currentStatus == ReservationStatus.PENDING && (newStatus == ReservationStatus.CONFIRMED || newStatus == ReservationStatus.CANCELLED)) || (currentStatus == ReservationStatus.CONFIRMED && newStatus == ReservationStatus.CANCELLED);

        if (!validTransition) {
            throw new BadRequestException("Invalid reservation status transition from " + currentStatus + " to " + newStatus);
        }
    }
}