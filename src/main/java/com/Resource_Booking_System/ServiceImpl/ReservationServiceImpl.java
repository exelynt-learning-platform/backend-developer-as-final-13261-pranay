package com.Resource_Booking_System.ServiceImpl;

import com.Resource_Booking_System.Dto.ReservationRequest;
import com.Resource_Booking_System.Dto.ReservationResponse;
import com.Resource_Booking_System.Dto.ReservationUpdateRequest;
import com.Resource_Booking_System.Entity.Reservation;
import com.Resource_Booking_System.Entity.ReservationStatus;
import com.Resource_Booking_System.Entity.Resource;
import com.Resource_Booking_System.Entity.User;
import com.Resource_Booking_System.Exception.BadRequestException;
import com.Resource_Booking_System.Exception.ReservationNotFoundException;
import com.Resource_Booking_System.Exception.ResourceNotFoundException;
import com.Resource_Booking_System.Exception.UsernameNotFoundException;
import com.Resource_Booking_System.IService.IReservationService;
import com.Resource_Booking_System.Repository.ReservationRepository;
import com.Resource_Booking_System.Repository.ResourceRepository;
import com.Resource_Booking_System.Repository.UserRepository;
import com.Resource_Booking_System.Specification.ReservationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReservationServiceImpl implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, UserRepository userRepository, ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }


    @Override
    public ReservationResponse createReservation(ReservationRequest reservationRequest, String username) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        Resource resource = resourceRepository.findById(reservationRequest.getResourceId()).orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + reservationRequest.getResourceId()));

        if (!resource.getAvailable()) {
            throw new BadRequestException("Resource is not available");
        }

        if (!reservationRequest.getStartTime().isBefore(reservationRequest.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        if (reservationRequest.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(reservationRequest.getStartTime());
        reservation.setEndTime(reservationRequest.getEndTime());
        reservation.setPrice(reservationRequest.getPrice());
        reservation.setStatus(ReservationStatus.PENDING);

        Reservation create = reservationRepository.save(reservation);

        ReservationResponse request = new ReservationResponse();

        request.setId(create.getId());
        request.setUserId(create.getUser().getId());
        request.setUsername(create.getUser().getUsername());
        request.setResourceId(create.getResource().getId());
        request.setResourceName(create.getResource().getName());
        request.setPrice(create.getPrice());
        request.setStartTime(create.getStartTime());
        request.setEndTime(create.getEndTime());
        request.setStatus(create.getStatus());

        return request;
    }

    @Override
    public ReservationResponse getReservationById(Long id, String username, boolean isAdmin) {

        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + id));

        if (!isAdmin && !reservation.getUser().getUsername().equals(username)) {
            throw new BadRequestException("Access denied: You can only view your own reservations");
        }

        ReservationResponse request = new ReservationResponse();

        request.setId(reservation.getId());
        request.setUserId(reservation.getUser().getId());
        request.setUsername(reservation.getUser().getUsername());
        request.setResourceId(reservation.getResource().getId());
        request.setResourceName(reservation.getResource().getName());
        request.setPrice(reservation.getPrice());
        request.setStartTime(reservation.getStartTime());
        request.setEndTime(reservation.getEndTime());
        request.setStatus(reservation.getStatus());

        return request;
    }

    @Override
    public Page<ReservationResponse> getAllReservation(
            ReservationStatus reservationStatus,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String username,
            boolean isAdmin,
            Pageable pageable)
    {

        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0)
        {
            throw new BadRequestException(
                    "Minimum price cannot be greater than maximum price"
            );
        }


        Long userId = null;

        if (!isAdmin) {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            userId = user.getId();
        }

        Specification<Reservation> spec = ReservationSpecification.filterReservations(userId, reservationStatus, minPrice, maxPrice);

        Page<Reservation> reservations = reservationRepository.findAll(spec, pageable);

        return reservations.map(reservation -> {

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
        });
    }

    @Override
    public ReservationResponse updateReservation(
            Long id,
            ReservationUpdateRequest updateRequest,
            String username,
            boolean isAdmin) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));


        if (!isAdmin)
        {
            throw new BadRequestException("Only ADMIN can update reservation");
        }

        if (!updateRequest.getStartTime().isBefore(updateRequest.getEndTime()))
        {
            throw new BadRequestException("Start time must be before end time");
        }


        if (updateRequest.getStartTime().isBefore(LocalDateTime.now()))
        {
            throw new BadRequestException("Start time must be in the future");
        }


        Resource resource = resourceRepository.findById(updateRequest.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + updateRequest.getResourceId()));

        if (!resource.getAvailable())
        {
            throw new BadRequestException("Resource is not available");
        }


        reservation.setResource(resource);
        reservation.setPrice(updateRequest.getPrice());
        reservation.setStartTime(updateRequest.getStartTime());
        reservation.setEndTime(updateRequest.getEndTime());
        reservation.setStatus(updateRequest.getStatus());

        Reservation updatedReservation = reservationRepository.save(reservation);

        ReservationResponse response = new ReservationResponse();

        response.setId(updatedReservation.getId());

        response.setUserId(updatedReservation.getUser().getId());

        response.setUsername(updatedReservation.getUser().getUsername());

        response.setResourceId(updatedReservation.getResource().getId());

        response.setResourceName(updatedReservation.getResource().getName());

        response.setPrice(updatedReservation.getPrice());

        response.setStartTime(updatedReservation.getStartTime());

        response.setEndTime(updatedReservation.getEndTime());

        response.setStatus(updatedReservation.getStatus());

        return response;
    }

    @Override
    public void deleteReservation(Long id, String username, boolean isAdmin) {

        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        if (!isAdmin) {
            throw new BadRequestException("Only ADMIN can delete reservation");
        }

        reservationRepository.delete(reservation);

    }
}
