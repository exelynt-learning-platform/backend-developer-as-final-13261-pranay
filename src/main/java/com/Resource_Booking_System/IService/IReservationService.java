package com.Resource_Booking_System.IService;

import com.Resource_Booking_System.Dto.ReservationRequest;
import com.Resource_Booking_System.Dto.ReservationResponse;
import com.Resource_Booking_System.Dto.ReservationUpdateRequest;
import com.Resource_Booking_System.Entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IReservationService {

    public ReservationResponse createReservation (ReservationRequest reservationRequest , String username);

    public ReservationResponse getReservationById (Long id, String username , boolean isAdmin);

    public Page<ReservationResponse> getAllReservation (
            ReservationStatus reservationStatus,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String username,
            boolean isAdmin,
            Pageable pageable
    );

    public ReservationResponse updateReservation (
            Long id,
           ReservationUpdateRequest reservationUpdateRequest,
            String username,
            boolean isAdmin
            );

    void deleteReservation(Long id, String username, boolean isAdmin);
}
