package com.resource_booking_system.IService;

import com.resource_booking_system.Dto.ReservationRequest;
import com.resource_booking_system.Dto.ReservationResponse;
import com.resource_booking_system.Dto.ReservationUpdateRequest;
import com.resource_booking_system.Entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IReservationService {

    ReservationResponse createReservation(ReservationRequest reservationRequest, String username);

    ReservationResponse getReservationById(Long id, String username, boolean isAdmin);

    Page<ReservationResponse> getAllReservation(ReservationStatus reservationStatus, BigDecimal minPrice, BigDecimal maxPrice, String username, boolean isAdmin, Pageable pageable);

    ReservationResponse updateReservation(Long id, ReservationUpdateRequest reservationUpdateRequest, boolean isAdmin);

    void deleteReservation(Long id, boolean isAdmin);
}