package com.finesse.utils;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.finesse.security.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieUtils {
      
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    public ResponseCookie createAccessTokenCookie(String token) {
      return ResponseCookie.from("access_token", token)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(this.jwtTokenProvider.getJwtExpirationMs() / 1000).build();

    }
    
    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/api/auth/refresh")
                    .maxAge(this.jwtTokenProvider.getRefreshExpirationMs() / 1000).build();
    }
    
    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from("access_token", null)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(0).build();
    }
    
    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", null)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/api/auth/refresh")
                    .maxAge(0).build();
    }
    
    public String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        
        return Arrays.stream(request.getCookies())
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}
