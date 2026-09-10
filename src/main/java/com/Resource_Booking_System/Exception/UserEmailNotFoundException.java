package com.Resource_Booking_System.Exception;

public class UserEmailNotFoundException extends  RuntimeException{

    public UserEmailNotFoundException(String message)
    {
        super(message);
    }
}
