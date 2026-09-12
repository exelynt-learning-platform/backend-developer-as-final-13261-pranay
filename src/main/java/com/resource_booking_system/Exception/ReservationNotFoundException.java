package com.resource_booking_system.Exception;

public class ReservationNotFoundException extends  RuntimeException{

    public ReservationNotFoundException(String message)
    {
        super(message);
    }
}
