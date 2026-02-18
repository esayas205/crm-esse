package com.esse.crm.security.controller;

import com.esse.crm.security.dto.AuthResponse;
import com.esse.crm.security.dto.LoginRequest;
import com.esse.crm.security.dto.RegisterRequest;
import com.esse.crm.security.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    @Value("${application.security.jwt.refresh-token.cookie-name}")
    private String cookieName;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        AuthResponse response = service.register(request, getDeviceInfo(servletRequest), getIpAddress(servletRequest));
        setRefreshTokenCookie(servletResponse, response.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        AuthResponse response = service.login(request, getDeviceInfo(servletRequest), getIpAddress(servletRequest));
        setRefreshTokenCookie(servletResponse, response.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            HttpServletResponse servletResponse
    ) {
        if (refreshTokenCookie != null) {
            service.logout(refreshTokenCookie);
        }
        
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            @RequestBody(required = false) String refreshTokenBody,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie : refreshTokenBody;
        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        AuthResponse response = service.refresh(refreshToken, getDeviceInfo(servletRequest), getIpAddress(servletRequest));
        setRefreshTokenCookie(servletResponse, response.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, refreshToken)
                .httpOnly(true)
                .secure(true) // Should be true in production
                .path("/")
                .maxAge(refreshTokenExpiration / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String getDeviceInfo(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private String getIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
