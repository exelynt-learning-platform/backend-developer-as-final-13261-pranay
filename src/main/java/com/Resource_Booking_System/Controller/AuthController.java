package com.Resource_Booking_System.Controller;

import com.Resource_Booking_System.Dto.AuthResponse;
import com.Resource_Booking_System.Dto.LoginRequest;
import com.Resource_Booking_System.Dto.SignUpRequest;
import com.Resource_Booking_System.IService.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/signup")
    public ResponseEntity<SignUpRequest> sign (@Valid @RequestBody SignUpRequest signUpRequest)
    {
        SignUpRequest signUpRequest1 = authService.signUp(signUpRequest);

        return ResponseEntity.ok(signUpRequest1);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@Valid @RequestBody LoginRequest loginRequest)
    {
        AuthResponse loginRequest1 = authService.loginUser(loginRequest);
        return ResponseEntity.ok(loginRequest1);
    }
}
