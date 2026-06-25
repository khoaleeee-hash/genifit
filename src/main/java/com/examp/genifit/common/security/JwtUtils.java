package com.examp.genifit.common.security;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    public Integer getUserId(Jwt jwt) {
        if (jwt == null) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Missing authentication token");
        }

        Object idClaim = jwt.getClaim("id");

        if (idClaim == null) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "User id not found in token"
        );
        }

        if (idClaim instanceof Integer userId) {
            return userId;
        }

        if (idClaim instanceof Long userId) {
            return userId.intValue();
        }

        return Integer.valueOf(idClaim.toString());
    }
}