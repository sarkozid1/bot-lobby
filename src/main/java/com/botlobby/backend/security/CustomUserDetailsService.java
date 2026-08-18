package com.botlobby.backend.security;

import com.botlobby.backend.entity.User;
import com.botlobby.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// A Spring Security "UserDetailsService" interface implementacioja - ez a hidalo osztaly a
// keret (ami csak UserDetails-t ismer) es a sajat adatbazisunk (User entitas) kozott. Ket
// helyen hivodik meg: (1) bejelentkezeskor a DaoAuthenticationProvider ezzel tolti be a
// felhasznalot email alapjan, hogy osszehasonlitsa a jelszo-hash-t; (2) minden vedett kerelemnel
// a JwtAuthenticationFilter ezzel tolti ujra be a felhasznalot a JWT-ben talalt email alapjan.
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // "username"-nek hivjak a Spring Security interface miatt, de ebben a projektben ez
    // valojaban az email cim (lasd UserPrincipal.getUsername()).
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nincs ilyen email cimmel felhasznalo: " + email));
        return UserPrincipal.from(user);
    }
}
