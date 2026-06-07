package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.AuthenticationRequest;
import com.examp.genifit.dto.request.IntrospectRequest;
import com.examp.genifit.dto.request.LogoutRequest;
import com.examp.genifit.dto.request.RefreshTokenRequest;
import com.examp.genifit.dto.response.AuthenticationResponse;
import com.examp.genifit.dto.response.IntrospectResponse;
import com.examp.genifit.entity.InvalidatedToken;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.InvalidatedTokenRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @Override
    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (ApiException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        boolean authenticated = passwordEncoder.matches(request.getPassword(),
                user.getPasswordHash());

        if (!authenticated)
            throw new ApiException(ErrorCode.UNAUTHENTICATED);

        var accessToken = generateToken(user);

        var refreshToken = generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("genifit.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", user.getRole())
                .claim("id", user.getUserId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("genifit.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(15, ChronoUnit.DAYS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getRefreshToken());

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        long daysUntilExpiration = ChronoUnit.DAYS.between(Instant.now(), expirationTime.toInstant());

        String newAccessToken = generateToken(user);
        String newRefreshToken = request.getRefreshToken();

        if (daysUntilExpiration <= 7) {
            newRefreshToken = generateRefreshToken(user);
        }

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        invalidateToken(request.getAccessToken());
        invalidateToken(request.getRefreshToken());
    }

    @Override
    public void invalidateToken(String token) throws ParseException, JOSEException {
        var signedJWT = verifyToken(token);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build());
    }

    @Override
    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date())))
            throw new ApiException(ErrorCode.UNAUTHENTICATED);

        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        if (invalidatedTokenRepository.existsById(jti)) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }
}
