package com.Resource_Booking_System.Dto;

import com.Resource_Booking_System.Entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;

    private Long resourceId;

    private String resourceName;

    private Long userId;

    private String username;

    private BigDecimal price;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private ReservationStatus status;

    public ReservationResponse() {
    }

    public ReservationResponse(Long id, Long resourceId, String resourceName, Long userId, String username, BigDecimal price, LocalDateTime startTime, LocalDateTime endTime, ReservationStatus status) {
        this.id = id;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.userId = userId;
        this.username = username;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
