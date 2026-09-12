package com.resource_booking_system.Controller;

import com.resource_booking_system.Configure.PageableValidator;
import com.resource_booking_system.Dto.ReservationRequest;
import com.resource_booking_system.Dto.ReservationResponse;
import com.resource_booking_system.Dto.ReservationStatusUpdateRequest;
import com.resource_booking_system.Dto.ReservationUpdateRequest;
import com.resource_booking_system.Entity.ReservationStatus;
import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Exception.InvalidCredentialsException;
import com.resource_booking_system.IService.IReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api/reservations")
@Validated
public class ReservationController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "price", "startTime", "endTime", "status");

    private final IReservationService reservationService;
    private final PageableValidator pageableValidator;

    public ReservationController(IReservationService reservationService, PageableValidator pageableValidator) {
        this.reservationService = reservationService;
        this.pageableValidator = pageableValidator;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest reservationRequest, @AuthenticationPrincipal UserDetails userDetails) {

        String username = requireUsername(userDetails);

        ReservationResponse response = reservationService.createReservation(reservationRequest, username);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable @Positive Long id, @AuthenticationPrincipal UserDetails userDetails) {

        String username = requireUsername(userDetails);
        boolean isAdmin = isAdmin(userDetails);

        ReservationResponse response = reservationService.getReservationById(id, username, isAdmin);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAllReservation(@RequestParam(required = false) ReservationStatus status, @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice, Pageable pageable, @AuthenticationPrincipal UserDetails userDetails) {

        String username = requireUsername(userDetails);
        boolean isAdmin = isAdmin(userDetails);

        Pageable safePageable = pageableValidator.validateAndSanitize(pageable, ALLOWED_SORT_FIELDS);

        Page<ReservationResponse> response = reservationService.getAllReservation(status, minPrice, maxPrice, username, isAdmin, safePageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(@PathVariable @Positive Long id, @Valid @RequestBody ReservationUpdateRequest request, @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = isAdmin(userDetails);

        ReservationResponse response = reservationService.updateReservation(id, request, isAdmin);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(@PathVariable @Positive Long id, @Valid @RequestBody ReservationStatusUpdateRequest request, @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = isAdmin(userDetails);

        ReservationResponse response = reservationService.updateReservationStatus(id, request, isAdmin);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable @Positive Long id, @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = isAdmin(userDetails);

        reservationService.deleteReservation(id, isAdmin);

        return ResponseEntity.noContent().build();
    }

    // ---------------- Private helpers ----------------

    private String requireUsername(UserDetails userDetails) {
        if (userDetails == null) {
            throw new InvalidCredentialsException("Authentication required");
        }
        return userDetails.getUsername();
    }

    private boolean isAdmin(UserDetails userDetails) {

        if (userDetails == null) {
            return false;
        }

        return userDetails.getAuthorities().stream().anyMatch(authority -> Role.ADMIN.name().equals(authority.getAuthority()));
    }
}