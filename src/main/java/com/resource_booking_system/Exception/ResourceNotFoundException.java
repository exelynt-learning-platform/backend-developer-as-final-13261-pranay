package com.resource_booking_system.Exception;

public class ResourceNotFoundException extends  RuntimeException{

    public ResourceNotFoundException (String message)
    {
        super(message);
    }
}
