package com.assignment.task.service;

import com.assignment.task.dto.LoginRequest;
import com.assignment.task.dto.RegisterRequest;
import com.assignment.task.dto.UserResponse;
import com.assignment.task.model.User;
import com.assignment.task.repository.UserRepository;
import com.assignment.task.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- REGISTER ----------

    @Test
    void register_success() {
        // Given
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "password");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("1");
            return user;
        });
        when(jwtUtil.generateToken(request.getEmail())).thenReturn("jwtToken");

        // When
        UserResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("jwtToken", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_fail_emailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "password");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // When + Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Email already exists", ex.getReason());
    }

    // ---------- LOGIN ----------

    @Test
    void login_success() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "password");

        User user = new User();
        user.setId("1");
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");

        Authentication authResult =
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authResult);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getEmail())).thenReturn("jwtToken");

        // When
        UserResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("jwtToken", response.getToken());
    }

    @Test
    void login_fail_invalidPassword() {
        // Given
        LoginRequest request = new LoginRequest("john@example.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When + Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Invalid email or password", ex.getReason());
    }

    @Test
    void login_fail_userNotFound() {
        // Given
        LoginRequest request = new LoginRequest("unknown@example.com", "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // When + Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());
    }

    // ---------- LOGOUT ----------

    @Test
    void logout_success() {
        // Given
        String token = "jwtToken123";

        // When
        String result = authService.logout(token);

        // Then
        assertEquals("Successfully logged out", result);
        assertTrue(authService.isTokenBlacklisted(token));
    }

    @Test
    void logout_fail_tokenMissing() {
        // When + Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.logout(""));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Token is required for logout", ex.getReason());
    }

    // ---------- BLACKLIST ----------

    @Test
    void isTokenBlacklisted_true() {
        // Given
        String token = "blacklistedToken";
        authService.logout(token);

        // When
        boolean result = authService.isTokenBlacklisted(token);

        // Then
        assertTrue(result);
    }

    @Test
    void isTokenBlacklisted_false() {
        // Given
        String token = "validToken";

        // When
        boolean result = authService.isTokenBlacklisted(token);

        // Then
        assertFalse(result);
    }
}
