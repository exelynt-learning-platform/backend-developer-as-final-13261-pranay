package com.resource_booking_system.IService;

import com.resource_booking_system.Dto.*;

public interface IAuthService {

    public SignUpResponse signUp(SignUpRequest signUpRequest);

    public AuthResponse loginUser (LoginRequest loginRequest);

    public AuthResponse refreshAccessToken(RefreshTokenRequest request);
}
