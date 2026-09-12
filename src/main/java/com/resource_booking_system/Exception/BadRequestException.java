package com.resource_booking_system.Exception;

public class BadRequestException extends  RuntimeException{

    public BadRequestException (String message)
    {
        super(message);
    }
}
