package com.botlobby.backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Ez a szuro fut le MINDEN egyes bejovo HTTP kerelemnel (lasd SecurityConfig, ahol
// .addFilterBefore(jwtAuthenticationFilter, ...) beallitja a Spring Security szuro-lancaba).
// A feladata: ha a kerelem "Authorization: Bearer <token>" fejlecet hordoz, ellenorzi a JWT-t,
// es ha ervenyes, "belejelentkezteti" a felhasznalot ERRE AZ EGY KERELEMRE - innentol a lanc
// tobbi resze (SecurityConfig.authorizeHttpRequests) mar tudja, hogy ki kuldte a kerest.
// OncePerRequestFilter: garantalja, hogy egy kerelemre pontosan egyszer fusson le (nem
// tobbszor, pl. belso forward/include eseten).
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Nincs (vagy nem "Bearer "-rel kezdodo) Authorization fejlec: nem a mi dolgunk -
        // egyszeruen tovabbengedjuk a kerelmet a lanc kovetkezo tagjanak. Ha a kert
        // eroforrashoz hitelesites kellett volna, a SecurityConfig ugyis 401/403-mal elutasitja kesobb.
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Levagjuk a "Bearer " elotagot, csak a nyers token-string marad.
        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            String email = jwtService.extractEmail(token);
            // SecurityContextHolder...getAuthentication() == null: ne allitsunk felul egy
            // mar korabban (pl. egy masik szuro altal) beallitott hitelesitest.
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails)) {
                    // Letrehozzuk a Spring Security sajat "ez a felhasznalo be van
                    // jelentkezve" objektumat (Authentication), es beallitjuk a
                    // SecurityContextHolder-be - ez a per-kerelem (thread-local) tarolo,
                    // amit a controllerek/@AuthenticationPrincipal innen olvasnak ki.
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // Ervenytelen/lejart token: nem allitunk be authentication-t, a securityFilterChain
            // magatol elutasitja a vedett endpointot (401/403).
        }

        // Barmi is tortent fent, a kerelemnek MINDIG tovabb kell mennie a lanc kovetkezo
        // tagjara - ez a szuro sosem all le itt, csak dontest hoz a hitelesitesrol.
        filterChain.doFilter(request, response);
    }
}
