package com.Resource_Booking_System.Exception;

public class BadRequestException extends  RuntimeException{

    public BadRequestException (String message)
    {
        super(message);
    }
}
