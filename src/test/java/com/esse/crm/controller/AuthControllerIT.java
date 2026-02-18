package com.esse.crm.controller;
import com.esse.crm.security.entity.AppUser;
import com.esse.crm.security.dto.LoginRequest;
import com.esse.crm.security.repository.AppUserRepository;
import com.esse.crm.security.repository.RefreshTokenRepository;
import com.esse.crm.security.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Given a user exists in DB
        var user = AppUser.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .roles(java.util.Set.of())
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void shouldRefreshSuccessfully() throws Exception {
        // Given
        var user = AppUser.builder()
                .username("refreshuser")
                .password(passwordEncoder.encode("password123"))
                .email("refresh@example.com")
                .roles(Set.of())
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .username("refreshuser")
                .password("password123")
                .build();

        var mvcResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie refreshTokenCookie = mvcResult.getResponse().getCookie("refresh_token");

        // When/Then
        mockMvc.perform(post("/api/auth/refresh")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldFailRefreshWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .content("invalid.token.structure"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDetectReuseAndRevokeFamily() throws Exception {
        // Given
        var user = AppUser.builder()
                .username("reuseuser")
                .password(passwordEncoder.encode("password123"))
                .email("reuse@example.com")
                .roles(Set.of())
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .username("reuseuser")
                .password("password123")
                .build();

        var mvcResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie refreshTokenCookie = mvcResult.getResponse().getCookie("refresh_token");

        // First refresh - OK
        mockMvc.perform(post("/api/auth/refresh")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .cookie(refreshTokenCookie))
                .andExpect(status().isOk());

        // Second refresh with SAME token - Should fail (Reuse detection)
        mockMvc.perform(post("/api/auth/refresh")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .cookie(refreshTokenCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {
        // Given a user exists
        var user = AppUser.builder()
                .username("testuser2")
                .password(passwordEncoder.encode("password123"))
                .email("test2@example.com")
                .roles(java.util.Set.of())
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser2")
                .password("wrongpassword")
                .build();

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessingProtectedResourceWithoutToken() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
