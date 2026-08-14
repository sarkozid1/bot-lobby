package com.botlobby.backend.service;

import com.botlobby.backend.dto.auth.AuthResponse;
import com.botlobby.backend.dto.auth.LoginRequest;
import com.botlobby.backend.dto.auth.RegisterRequest;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.exception.ConflictException;
import com.botlobby.backend.repository.UserRepository;
import com.botlobby.backend.security.JwtService;
import com.botlobby.backend.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenEmailNotTaken_savesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Kiss Peter", "peter@example.com", "password123", "Budapest", null);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("peter@example.com");
        assertThat(response.name()).isEqualTo("Kiss Peter");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_whenEmailAlreadyTaken_throwsConflict() {
        RegisterRequest request = new RegisterRequest("Kiss Peter", "peter@example.com", "password123", null, null);

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_whenCredentialsValid_returnsToken() {
        LoginRequest request = new LoginRequest("peter@example.com", "password123");
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Kiss Peter")
                .email("peter@example.com")
                .passwordHash("hashed")
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(user.getId());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_whenAuthenticationManagerRejects_propagatesException() {
        LoginRequest request = new LoginRequest("peter@example.com", "wrong-password");

        doThrow(new BadCredentialsException("bad credentials")).when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
    }
}
