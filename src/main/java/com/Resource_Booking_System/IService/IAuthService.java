package com.Resource_Booking_System.IService;

import com.Resource_Booking_System.Dto.AuthResponse;
import com.Resource_Booking_System.Dto.LoginRequest;
import com.Resource_Booking_System.Dto.SignUpRequest;
import com.Resource_Booking_System.Dto.SignUpResponse;

public interface IAuthService {

    public SignUpResponse signUp(SignUpRequest signUpRequest);

    public AuthResponse loginUser (LoginRequest loginRequest);
}
