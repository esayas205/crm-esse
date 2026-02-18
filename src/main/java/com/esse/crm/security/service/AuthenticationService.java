package com.esse.crm.security.service;

import com.esse.crm.exception.ConflictException;
import com.esse.crm.security.dto.AuthResponse;
import com.esse.crm.security.dto.LoginRequest;
import com.esse.crm.security.dto.RegisterRequest;
import com.esse.crm.security.entity.AppUser;
import com.esse.crm.security.repository.AppUserRepository;
import com.esse.crm.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request, String deviceInfo, String ipAddress) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use");
        }

        var user = AppUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of())
                .build();

        userRepository.save(user);
        return createAuthResponse(user, deviceInfo, ipAddress);
    }

    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsernameWithAuthorities(request.getUsername())
                .orElseThrow();
        return createAuthResponse(user, deviceInfo, ipAddress);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken, String deviceInfo, String ipAddress) {
        var result = refreshTokenService.rotateToken(refreshToken);
        var user = result.getRefreshToken().getUser();
        
        var jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(result.getRawToken())
                .username(user.getUsername())
                .authorities(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    private AuthResponse createAuthResponse(AppUser user, String deviceInfo, String ipAddress) {
        var jwtToken = jwtService.generateToken(user);
        var refreshTokenResult = refreshTokenService.generateRefreshToken(user, deviceInfo, ipAddress, null);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshTokenResult.getRawToken())
                .username(user.getUsername())
                .authorities(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }
}
