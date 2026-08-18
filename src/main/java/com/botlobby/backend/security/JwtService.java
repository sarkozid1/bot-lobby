package com.botlobby.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

// A tenyleges JWT (JSON Web Token) letrehozasat es ellenorzeset vegzi, a jjwt konyvtarral
// (io.jsonwebtoken.*). Egy JWT harom, pontokkal elvalasztott, Base64-kodolt reszbol all:
// fejlec.payload.alairas - itt a payload-ba csak az email cim (subject) es a lejarati ido
// kerul, az alairas pedig HMAC-SHA algoritmussal, a JWT_SECRET kulcsot hasznalva biztositja,
// hogy a tokent senki ne tudja meghamisitani anelkul, hogy ismerne a titkot.
@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    // Az alairashoz/ellenorzeshez hasznalt kulcs - a nyers String titokbol (JWT_SECRET env
    // valtozo) generalodik egyszer, induláskor (lasd init() lent), nem minden hivasnal ujra.
    private SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // @PostConstruct: Spring ezt a metodust automatikusan meghivja kozvetlenul azutan, hogy
    // ez a Bean letrejott ES minden fuggosege (jwtProperties, benne a JWT_SECRET erteke) mar
    // be van fecskendezve - igy biztos, hogy a signingKey generalasakor mar rendelkezesre all a titok.
    @PostConstruct
    void init() {
        signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    // Uj tokent allit elo egy bejelentkezett/regisztralt felhasznalohoz - ezt hivja az
    // AuthService sikeres regisztracio/bejelentkezes utan. A token "subject" (alany) mezoje
    // az email cim, a lejarat pedig a jwtProperties.expirationMs()-bol szamolt jovobeli idopont.
    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.expirationMs())))
                .signWith(signingKey)
                .compact();
    }

    // Kicsomagolja a tokenbol az email cimet (a "subject" mezot) - ezt hasznalja a
    // JwtAuthenticationFilter, hogy tudja, kinek a nevben kell a kerelmet feldolgozni.
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Ervenyes-e a token EGY KONKRET felhasznalora nezve: az alaírás/lejarat ellenorzese
    // MELLETT azt is ellenorzi, hogy a tokenben levo email tenyleg egyezik-e a betoltott
    // userDetails email cimevel (extra vedelmi retegkent, bar a ket dolog gyakorlatilag mindig
    // egyutt jar). Ha a token akar csak egyetlen bittel is meg lett hamisítva vagy lejart,
    // a parseClaims(...) JwtException-t dob, amit itt elkapunk es false-szal jelzunk vissza.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    // A token dekodolasa+alairas-ellenorzese egy helyen - ha a token alairasa nem egyezik a
    // signingKey-vel szamolt vart ertekkel (pl. mert valaki mas titokkal probalta alairni,
    // vagy a tartalmat modositottak), a jjwt konyvtar itt dob JwtException-t.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
