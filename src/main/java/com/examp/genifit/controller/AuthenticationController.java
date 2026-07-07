package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.*;
import com.examp.genifit.dto.response.AuthenticationResponse;
import com.examp.genifit.dto.response.IntrospectResponse;
import com.examp.genifit.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.ParseException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        return ApiResponse.success(
                "Logged in successfully!",
                authenticationService.authenticate(request)
        );
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.success(
                "Refreshed successfully!",
                authenticationService.refreshToken(request)
        );
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.success(
                "Introspecting successfully!",
                authenticationService.introspect(request)
        );
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.success("Logged out successfully!", null);
    }

    @PostMapping("/google")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(@RequestBody @Valid GoogleLoginRequest request) {
        AuthenticationResponse response = authenticationService.authenticateWithGoogle(request.getIdToken());
        return ApiResponse.success("Logged in Successfully!", response);
    }

    @PostMapping("/guest-login")
    public ApiResponse<AuthenticationResponse> loginAsGuest(@RequestBody @Valid GuestLoginRequest request) {
        return ApiResponse.success(
                "Logged in Successfully!",
                authenticationService.loginAsGuest(request)
        );
    }
}
