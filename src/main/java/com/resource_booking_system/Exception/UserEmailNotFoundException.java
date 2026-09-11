package com.resource_booking_system.Exception;

public class UserEmailNotFoundException extends  RuntimeException{

    public UserEmailNotFoundException(String message)
    {
        super(message);
    }
}
