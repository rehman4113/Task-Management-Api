package com.assignment.task.controller;

import com.assignment.task.dto.LoginRequest;
import com.assignment.task.dto.RegisterRequest;
import com.assignment.task.dto.UserResponse;
import com.assignment.task.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    // -------- SIGNUP --------
    @Test
    void signup_success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "password123");
        UserResponse response = new UserResponse("user1", "John", "john@example.com","jwt-token-123");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // When + Then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user1"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    // -------- LOGIN --------
    @Test
    void login_success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        UserResponse response = new UserResponse("user1", "John", "john@example.com", "jwt-token-123");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // When + Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user1"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }


    // -------- LOGOUT --------
    @Test
    void logout_success() throws Exception {
        // Given
        String token = "Bearer dummyToken";

        // When + Then
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));
    }
}
