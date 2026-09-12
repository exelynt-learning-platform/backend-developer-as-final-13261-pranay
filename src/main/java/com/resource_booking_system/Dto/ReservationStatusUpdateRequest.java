package com.resource_booking_system.Dto;

import com.resource_booking_system.Entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public class ReservationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ReservationStatus status;

    public ReservationStatusUpdateRequest() {
    }

    public ReservationStatusUpdateRequest(ReservationStatus status) {
        this.status = status;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}