package com.botlobby.backend.service;

import com.botlobby.backend.dto.auth.AuthResponse;
import com.botlobby.backend.dto.auth.LoginRequest;
import com.botlobby.backend.dto.auth.RegisterRequest;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.exception.ConflictException;
import com.botlobby.backend.exception.ResourceNotFoundException;
import com.botlobby.backend.repository.UserRepository;
import com.botlobby.backend.security.JwtService;
import com.botlobby.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Ez az email cim mar hasznalatban van.");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .city(request.city())
                .profilePictureUrl(request.profilePictureUrl())
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(UserPrincipal.from(saved));

        return AuthResponse.bearer(token, saved.getId(), saved.getName(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen felhasznalo."));

        String token = jwtService.generateToken(UserPrincipal.from(user));

        return AuthResponse.bearer(token, user.getId(), user.getName(), user.getEmail());
    }
}
