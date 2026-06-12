package com.examp.genifit.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface GoogleAuthService {
    GoogleIdToken.Payload verifyToken(String idTokenString) throws Exception;
}
