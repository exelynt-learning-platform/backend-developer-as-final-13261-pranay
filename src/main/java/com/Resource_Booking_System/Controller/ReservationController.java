package com.Resource_Booking_System.Controller;

import com.Resource_Booking_System.Dto.ReservationRequest;
import com.Resource_Booking_System.Dto.ReservationResponse;
import com.Resource_Booking_System.Dto.ReservationUpdateRequest;
import com.Resource_Booking_System.Entity.ReservationStatus;
import com.Resource_Booking_System.IService.IReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final IReservationService reservationService;

    public ReservationController(IReservationService reservationService) {
        this.reservationService = reservationService;
    }


    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest reservationRequest, @AuthenticationPrincipal UserDetails userDetails) {

        ReservationResponse response = reservationService.createReservation(reservationRequest, userDetails.getUsername());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = userDetails.getAuthorities()
                .stream().
                anyMatch(a -> a.getAuthority().equals("ADMIN"));

        ReservationResponse response = reservationService.getReservationById(id, userDetails.getUsername(), isAdmin);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAllReservation(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails)
    {

        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        Page<ReservationResponse> response = reservationService.getAllReservation(status, minPrice, maxPrice, userDetails.getUsername(), isAdmin, pageable);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails)
    {

        ReservationResponse response = reservationService.updateReservation(id, request, userDetails.getUsername(), true);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReservation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        reservationService.deleteReservation(id, userDetails.getUsername(), true);

        return  ResponseEntity.ok("Delete SuccessFully !");
    }
}