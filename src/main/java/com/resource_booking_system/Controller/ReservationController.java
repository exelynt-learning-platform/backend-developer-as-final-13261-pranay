package com.resource_booking_system.Controller;

import com.resource_booking_system.Dto.ReservationRequest;
import com.resource_booking_system.Dto.ReservationResponse;
import com.resource_booking_system.Dto.ReservationUpdateRequest;
import com.resource_booking_system.Entity.ReservationStatus;
import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.IService.IReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final int MAX_PAGE_SIZE = 50;

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

        boolean isAdmin = isAdmin(userDetails);

        ReservationResponse response = reservationService.getReservationById(id, userDetails.getUsername(), isAdmin);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAllReservation(
            @RequestParam(required = false) ReservationStatus status,

            @RequestParam(required = false) BigDecimal minPrice,

            @RequestParam(required = false) BigDecimal maxPrice,

            Pageable pageable,

            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = isAdmin(userDetails);

        Pageable safePageable = createSafePageable(pageable);

        Page<ReservationResponse> response = reservationService.getAllReservation(status, minPrice, maxPrice, userDetails.getUsername(), isAdmin, safePageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(@PathVariable Long id, @Valid @RequestBody ReservationUpdateRequest request, @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = isAdmin(userDetails);

        ReservationResponse response = reservationService.updateReservation(id, request, isAdmin);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = isAdmin(userDetails);

        reservationService.deleteReservation(id, isAdmin);

        return ResponseEntity.noContent().build();
    }



    private boolean isAdmin(UserDetails userDetails) {

        return userDetails.getAuthorities().stream().anyMatch(authority -> Role.ADMIN.name().equals(authority.getAuthority()));
    }

    private Pageable createSafePageable(Pageable pageable) {

        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);

        return PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());
    }
}