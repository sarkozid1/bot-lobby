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

// "Service reteg": ide kerul az uzleti logika, a controller csak beolvassa a HTTP kerelmet es
// ide delegal. A @RequiredArgsConstructor (Lombok) minden "final" mezohoz general egy
// konstruktor-parametert - Spring ezen a konstruktoron keresztul fecskendezi be a fuggosegeket
// (UserRepository, PasswordEncoder, stb.), nincs kulon @Autowired mezo-annotaciora szukseg.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // @Transactional: az egesz metodus egyetlen adatbazis-tranzakcioban fut - ha barhol
    // kivetel repul (pl. mert kozben valaki masik szal beszurta ugyanazt az email cimet),
    // a mar elvegzett valtoztatasok automatikusan visszagorgetodnek (rollback).
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Elozetes, felhasznalobarat ellenorzes - enelkul is elkapna az adatbazis a UNIQUE
        // constraint-en keresztul, de az egy csunya, technikai SQL-hibaval terne vissza.
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Ez az email cim mar hasznalatban van.");
        }

        // A Lombok @Builder altal generalt "folyekony" (fluent) epito minta - olvashatobb,
        // mint egy hosszu konstruktorhivas sok pozicionalis parameterrel.
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                // A NYERS jelszo SOSEM kerul mentesre - itt, meg a User letrehozasakor
                // hash-elodik BCrypt-tel (passwordEncoder.encode). A hash egyiranyu: a
                // nyers jelszo utolag nem allithato vissza belole.
                .passwordHash(passwordEncoder.encode(request.password()))
                .city(request.city())
                .profilePictureUrl(request.profilePictureUrl())
                .build();

        User saved = userRepository.save(user);
        // Regisztracio utan azonnal bejelentkezettnek szamit a felhasznalo - sajat JWT-t kap,
        // nincs kulon "erositsd meg az email cimed, majd jelentkezz be" lepes ebben a projektben.
        String token = jwtService.generateToken(UserPrincipal.from(saved));

        return AuthResponse.bearer(token, saved.getId(), saved.getName(), saved.getEmail());
    }

    // Nincs @Transactional ezen a metoduson: nem ir az adatbazisba (csak olvas), es az
    // authenticationManager.authenticate(...) sajat maga kezeli a szukseges hozzaferest.
    public AuthResponse login(LoginRequest request) {
        // Ez a sor vegzi a tenyleges ellenorzest: belul meghivja a CustomUserDetailsService-t
        // (email alapjan betolti a felhasznalot), majd a SecurityConfig-ban regisztralt
        // PasswordEncoder-rel osszehasonlitja a beirt jelszo hash-et a tarolt hash-sel.
        // Ha nem egyeznek, a metodus BadCredentialsException-t dob (lasd GlobalExceptionHandler).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Ha idaig eljutottunk, a hitelesites sikeres volt - most mar ujra be kell tolteni a
        // User entitast (az authenticate(...) hivas nem adta vissza kozvetlenul), hogy az
        // azonositojabol/nevebol JWT-t es AuthResponse-t tudjunk keszíteni.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen felhasznalo."));

        String token = jwtService.generateToken(UserPrincipal.from(user));

        return AuthResponse.bearer(token, user.getId(), user.getName(), user.getEmail());
    }
}
