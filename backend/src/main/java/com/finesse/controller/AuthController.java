package com.finesse.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finesse.dto.auth.LoginRequestRecord;
import com.finesse.entity.Usuario;
import com.finesse.security.CustomUserDetailsService;
import com.finesse.security.JwtTokenProvider;
import com.finesse.utils.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CookieUtils cookieUtils;
    

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestRecord loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.senha())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Usuario userPrincipal = (Usuario) authentication.getPrincipal();

            String accessToken = this.jwtTokenProvider.generateToken(authentication);
            String refreshToken = this.jwtTokenProvider.generateRefreshTokenForUserId(userPrincipal.getId());

            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            Map<String, Object> body = new HashMap<>();
            body.put("usuario", Map.of(
                "id", userPrincipal.getId(),
                "nome", userPrincipal.getNome(),
                "email", userPrincipal.getEmail(),
                "roles", roles
            ));

            ResponseCookie accessCookie = this.cookieUtils.createAccessTokenCookie(accessToken);
            ResponseCookie refreshCookie = this.cookieUtils.createRefreshTokenCookie(refreshToken);

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return ResponseEntity.ok(body);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email ou senha incorretos"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse httpResponse) {
        SecurityContextHolder.clearContext();
        
        ResponseCookie accessDel = this.cookieUtils.deleteAccessTokenCookie();
        ResponseCookie refreshDel = this.cookieUtils.deleteRefreshTokenCookie();
        
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessDel.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshDel.toString());
    
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = this.cookieUtils.extractTokenFromCookie(request, "refresh_token");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token não encontrado"));
        }

        try {
            //valida primeiro
            if (!this.jwtTokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token inválido ou expirado"));
            }

            //depois extrai userId
            Long userId = this.jwtTokenProvider.getUserIdFromToken(refreshToken);
            Usuario userDetails = (Usuario) this.userDetailsService.loadUserById(userId);

            String newAccessToken = this.jwtTokenProvider.generateToken(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
            );

            ResponseCookie accessCookie = this.cookieUtils.createAccessTokenCookie(newAccessToken);
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

            return ResponseEntity.ok(Map.of("message", "Token renovado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token inválido"));
        }
    }


    @GetMapping("/check")
    public ResponseEntity<?> checkSession() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se está autenticado
        if (authentication == null 
                || !authentication.isAuthenticated() 
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        // Verifica se o principal é um Usuario
        if (!(authentication.getPrincipal() instanceof Usuario user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        // Extrai roles
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "usuario", Map.of(
                        "id", user.getId(),
                        "nome", user.getNome(),
                        "email", user.getEmail(),
                        "roles", roles
                )
        ));
    }
}
