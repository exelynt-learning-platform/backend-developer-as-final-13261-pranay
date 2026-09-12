package com.resource_booking_system.ServiceImpl;

import com.resource_booking_system.Dto.ReservationRequest;
import com.resource_booking_system.Dto.ReservationResponse;
import com.resource_booking_system.Dto.ReservationStatusUpdateRequest;
import com.resource_booking_system.Dto.ReservationUpdateRequest;
import com.resource_booking_system.Entity.Reservation;
import com.resource_booking_system.Entity.ReservationStatus;
import com.resource_booking_system.Entity.Resource;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Exception.BadRequestException;
import com.resource_booking_system.Exception.ForbiddenException;
import com.resource_booking_system.Exception.InvalidCredentialsException;
import com.resource_booking_system.Exception.ReservationNotFoundException;
import com.resource_booking_system.Exception.ResourceNotFoundException;
import com.resource_booking_system.IService.IReservationService;
import com.resource_booking_system.Repository.ReservationRepository;
import com.resource_booking_system.Repository.ResourceRepository;
import com.resource_booking_system.Repository.UserRepository;
import com.resource_booking_system.Specification.ReservationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class ReservationServiceImpl implements IReservationService {

    private static final String ONLY_ADMIN_UPDATE = "Only ADMIN can update reservation";
    private static final String ONLY_ADMIN_DELETE = "Only ADMIN can delete reservation";
    private static final String ONLY_ADMIN_STATUS = "Only ADMIN can change reservation status";
    private static final String OVERLAP_MESSAGE = "Resource is already booked for the selected time";
    private static final String ACCESS_DENIED_VIEW = "Access denied: You can only view your own reservations";

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

        User user = findUserByUsername(username);
        Resource resource = findResourceById(reservationRequest.getResourceId());

        validateReservationTime(reservationRequest.getStartTime(), reservationRequest.getEndTime(), ReservationStatus.PENDING);
        validateResourceAvailability(resource);
        validateNoOverlap(resource.getId(), reservationRequest.getStartTime(), reservationRequest.getEndTime());

        Reservation reservation = buildReservation(user, resource, reservationRequest);

        return mapToResponse(reservationRepository.save(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, String username, boolean isAdmin) {

        Reservation reservation = findReservationById(id);

        if (!isAdmin && !reservation.getUser().getUsername().equals(username)) {
            throw new ForbiddenException(ACCESS_DENIED_VIEW);
        }

        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getAllReservation(ReservationStatus reservationStatus, BigDecimal minPrice, BigDecimal maxPrice, String username, boolean isAdmin, Pageable pageable) {

        validatePriceRange(minPrice, maxPrice);

        Long userId = null;
        if (!isAdmin) {
            User user = findUserByUsername(username);
            userId = user.getId();
        }

        Specification<Reservation> spec = ReservationSpecification.filterReservations(userId, reservationStatus, minPrice, maxPrice);

        return reservationRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest updateRequest, boolean isAdmin) {

        validateAdminAccess(isAdmin, ONLY_ADMIN_UPDATE);

        Reservation reservation = findReservationById(id);

        Resource resource = validateAndResolveUpdate(reservation, updateRequest);

        applyUpdate(reservation, updateRequest, resource);

        return mapToResponse(reservationRepository.save(reservation));
    }

    @Override
    public ReservationResponse updateReservationStatus(Long id, ReservationStatusUpdateRequest statusUpdateRequest, boolean isAdmin) {

        validateAdminAccess(isAdmin, ONLY_ADMIN_STATUS);

        Reservation reservation = findReservationById(id);

        validateStatusTransition(reservation.getStatus(), statusUpdateRequest.getStatus(), reservation.getStartTime());

        reservation.setStatus(statusUpdateRequest.getStatus());

        return mapToResponse(reservationRepository.save(reservation));
    }

    @Override
    public void deleteReservation(Long id, boolean isAdmin) {

        validateAdminAccess(isAdmin, ONLY_ADMIN_DELETE);
        reservationRepository.delete(findReservationById(id));
    }

    private Resource validateAndResolveUpdate(Reservation reservation, ReservationUpdateRequest updateRequest) {

        validateReservationTime(updateRequest.getStartTime(), updateRequest.getEndTime(), updateRequest.getStatus());

        validateStatusTransition(reservation.getStatus(), updateRequest.getStatus(), updateRequest.getStartTime());

        Resource resource = findResourceById(updateRequest.getResourceId());
        validateResourceAvailability(resource);

        validateNoOverlapForUpdate(resource.getId(), reservation.getId(), updateRequest.getStartTime(), updateRequest.getEndTime());

        return resource;
    }

    private void applyUpdate(Reservation reservation, ReservationUpdateRequest updateRequest, Resource resource) {
        reservation.setResource(resource);
        reservation.setPrice(updateRequest.getPrice());
        reservation.setStartTime(updateRequest.getStartTime());
        reservation.setEndTime(updateRequest.getEndTime());
        reservation.setStatus(updateRequest.getStatus());
    }

    private Reservation buildReservation(User user, Resource resource, ReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());
        reservation.setStatus(ReservationStatus.PENDING);
        return reservation;
    }

    // ---------------- Lookup helpers ----------------

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new InvalidCredentialsException("Authenticated user no longer exists"));
    }

    private Resource findResourceById(Long resourceId) {
        return resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + resourceId));
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException("Reservation not found with ID: " + id));
    }

    // ---------------- Validation helpers ----------------

    private void validateAdminAccess(boolean isAdmin, String errorMessage) {
        if (!isAdmin) {
            throw new ForbiddenException(errorMessage);
        }
    }

    private void validateResourceAvailability(Resource resource) {
        if (Boolean.FALSE.equals(resource.getAvailable())) {
            throw new BadRequestException("Resource is not available for booking");
        }
    }

    private void validateNoOverlap(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean overlap = reservationRepository.existsOverlappingReservation(resourceId, ReservationStatus.CANCELLED, startTime, endTime);
        if (overlap) {
            throw new BadRequestException(OVERLAP_MESSAGE);
        }
    }

    private void validateNoOverlapForUpdate(Long resourceId, Long reservationId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean overlap = reservationRepository.existsOverlappingReservationExcludingId(resourceId, reservationId, ReservationStatus.CANCELLED, startTime, endTime);
        if (overlap) {
            throw new BadRequestException(OVERLAP_MESSAGE);
        }
    }

    private void validateReservationTime(LocalDateTime startTime, LocalDateTime endTime, ReservationStatus status) {

        if (startTime == null || endTime == null) {
            throw new BadRequestException("Start time and end time cannot be null");
        }

        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }

        LocalDateTime now = LocalDateTime.now();

        if (status == ReservationStatus.CONFIRMED) {
            if (!startTime.isAfter(now)) {
                throw new BadRequestException("Confirmed reservation start time must be in the future");
            }
        } else {
            if (!startTime.isAfter(now)) {
                throw new BadRequestException("Start time must be in the future");
            }
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price");
        }
    }

    private void validateStatusTransition(ReservationStatus currentStatus, ReservationStatus newStatus, LocalDateTime reservationStartTime) {

        if (currentStatus == null || newStatus == null) {
            throw new BadRequestException("Reservation status cannot be null");
        }

        if (currentStatus == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Cancelled reservation cannot be modified");
        }

        if (currentStatus == newStatus) {
            return;
        }

        if (reservationStartTime == null) {
            throw new BadRequestException("Reservation start time cannot be null");
        }

        if (!reservationStartTime.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Reservation status cannot be changed after the reservation has started");
        }

        boolean valid = (currentStatus == ReservationStatus.PENDING && (newStatus == ReservationStatus.CONFIRMED || newStatus == ReservationStatus.CANCELLED)) || (currentStatus == ReservationStatus.CONFIRMED && newStatus == ReservationStatus.CANCELLED);

        if (!valid) {
            throw new BadRequestException("Invalid reservation status transition from " + currentStatus + " to " + newStatus);
        }
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
}