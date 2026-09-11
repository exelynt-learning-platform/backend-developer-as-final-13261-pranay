package com.Resource_Booking_System.Exception;

public class ResourceNotFoundException extends  RuntimeException{

    public ResourceNotFoundException (String message)
    {
        super(message);
    }
}
