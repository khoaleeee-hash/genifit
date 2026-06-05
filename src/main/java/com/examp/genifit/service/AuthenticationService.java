package com.examp.genifit.service;

import com.examp.genifit.dto.request.AuthenticationRequest;
import com.examp.genifit.dto.request.IntrospectRequest;
import com.examp.genifit.dto.request.LogoutRequest;
import com.examp.genifit.dto.request.RefreshTokenRequest;
import com.examp.genifit.dto.response.AuthenticationResponse;
import com.examp.genifit.dto.response.IntrospectResponse;
import com.examp.genifit.entity.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    AuthenticationResponse authenticate(AuthenticationRequest request);
    String generateToken(User user);
    String generateRefreshToken(User user);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException;
    void invalidateToken(String token) throws ParseException, JOSEException;
    SignedJWT verifyToken(String token) throws JOSEException, ParseException;;
//    AuthenticationResponse refreshToken(RefreshRequest request);

}
