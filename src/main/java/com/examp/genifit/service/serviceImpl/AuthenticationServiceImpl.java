package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.*;
import com.examp.genifit.dto.response.AuthenticationResponse;
import com.examp.genifit.dto.response.IntrospectResponse;
import com.examp.genifit.entity.InvalidatedToken;
import com.examp.genifit.entity.OtpToken;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserRole;
import com.examp.genifit.repository.InvalidatedTokenRepository;
import com.examp.genifit.repository.OtpTokenRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.AuthenticationService;
import com.examp.genifit.service.GoogleAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;
    GoogleAuthService googleAuthService;
    OtpTokenRepository otpTokenRepository;

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
        var user = userRepository.findByUsernameAndIsActiveTrue(request.getUsername())
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
                .claim("scope",user.getRole())
                .claim("id", user.getUserId())
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
        var user = userRepository.findByUsernameAndIsActiveTrue(username)
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

    @Override
    @Transactional
    public AuthenticationResponse authenticateWithGoogle(String idTokenString) {
        try {
            GoogleIdToken.Payload payload = googleAuthService.verifyToken(idTokenString);
            String email = payload.getEmail();
            Optional<User> existingUserOpt = userRepository.findByEmail(email);
            User user;
            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                if (!user.getIsActive()) {
                    throw new ApiException(ErrorCode.USER_BANNED);
                }
            } else {
                user = new User();
                user.setUsername(email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 5));
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRole(UserRole.MEMBER);
                user.setIsActive(true);
                user = userRepository.save(user);
            }

            String accessToken = generateToken(user);
            String refreshToken = generateRefreshToken(user);

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .authenticated(true)
                    .build();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google authentication failed", e);
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    @Transactional
    public AuthenticationResponse loginAsGuest(GuestLoginRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(ErrorCode.USER_EXISTED, "Tên này đã có người sử dụng, vui lòng chọn tên khác.");
        }

        User newGuest = new User();
        newGuest.setUsername(request.getUsername());
        newGuest.setEmail(null);
        newGuest.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newGuest.setRole(UserRole.GUEST);
        newGuest.setIsActive(true);
        userRepository.save(newGuest);

        String accessToken = generateToken(newGuest);
        String refreshToken = generateRefreshToken(newGuest);
        /*access token*/
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse upgradeGuestToMember(CreateUserFromGuestRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User guestUser = userRepository.findByUsernameAndIsActiveTrue(currentUsername)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (guestUser.getRole() != UserRole.GUEST) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Chỉ tài khoản Khách (Guest) mới có thể nâng cấp lên Thành viên.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.USER_EXISTED, "Email này đã được đăng ký trong hệ thống.");
        }

        OtpToken validOtp = otpTokenRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_OTP));

        if (validOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.OTP_EXPIRED);
        }

        guestUser.setEmail(request.getEmail());
        guestUser.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        guestUser.setRole(UserRole.MEMBER);

        userRepository.save(guestUser);
        otpTokenRepository.delete(validOtp);

        String accessToken = generateToken(guestUser);
        String refreshToken = generateRefreshToken(guestUser);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }
}
