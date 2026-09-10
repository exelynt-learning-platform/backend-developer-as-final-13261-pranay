package com.Resource_Booking_System.Exception;

public class ReservationNotFoundException extends  RuntimeException{

    public ReservationNotFoundException(String message)
    {
        super(message);
    }
}
