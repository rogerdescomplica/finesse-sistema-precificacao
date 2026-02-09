package com.finesse.utils;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.finesse.security.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieUtils {
      
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${app.security.cookie.secure:false}")
    private boolean secure;
    
    @Value("${app.security.cookie.http-only:true}")
    private boolean httpOnly;
    
    @Value("${app.security.cookie.same-site:Lax}")
    private String sameSite;

    private static final String API_PATH = "/api";
    
    public ResponseCookie createAccessTokenCookie(String token) {
      return ResponseCookie.from("access_token", token)
                    .httpOnly(httpOnly)
                    .secure(secure)
                    .sameSite(sameSite)
                    //.path("/")
                    .path(API_PATH)
                    .maxAge(this.jwtTokenProvider.getJwtExpirationMs() / 1000).build();

    }
    
    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                    .httpOnly(httpOnly)
                    .secure(secure)
                    .sameSite(sameSite)
                    //.path("/api/auth/refresh")
                    .path(API_PATH)
                    .maxAge(this.jwtTokenProvider.getRefreshExpirationMs() / 1000).build();
    }
    
    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from("access_token", null)
                    .httpOnly(httpOnly)
                    .secure(secure)
                    .sameSite(sameSite)
                    //.path("/")
                    .path(API_PATH)
                    .maxAge(0).build();
    }
    
    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                    .httpOnly(httpOnly)
                    .secure(secure)
                    .sameSite(sameSite)
                    //.path("/api/auth/refresh")
                    .path(API_PATH)
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
