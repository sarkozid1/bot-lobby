package com.botlobby.backend.security;

import com.botlobby.backend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Nincs kulon role/authority a Useren (lasd korabbi dontes) - minden hitelesitett
 * felhasznalo ugyanazt az egy ROLE_USER authority-t kapja.
 */
// A Spring Security "UserDetails" interface az implementacioja - a security-keret ezt az
// objektumot hasznalja belul a "ki van bejelentkezve, mi a jelszo-hash-e, milyen jogai vannak"
// kerdesek megvalaszolasara. Ez NEM a JPA User entitas maga (szandekosan): a security-reteg
// csak azt lassa a felhasznalobol, amire tenylegesen szuksege van (id/email/passwordHash),
// nem az egesz entitast az osszes JPA-kapcsolataval egyutt.
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String passwordHash;

    // Privat konstruktor - kivulrol csak a from(...) factory metoduson keresztul hozhato letre.
    private UserPrincipal(UUID id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Egy betoltott User entitasbol keszit UserPrincipal-t - ezt hivja a
    // CustomUserDetailsService.loadUserByUsername es az AuthService is minden bejelentkezes/
    // regisztracio utan, amikor JWT-t kell generalni.
    public static UserPrincipal from(User user) {
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash());
    }

    // Sajat kiegeszito getter (nem resze a UserDetails interface-nek) - a controllerek innen
    // kerik ki a bejelentkezett felhasznalo UUID-jat (lasd @AuthenticationPrincipal hasznalatat).
    public UUID getId() {
        return id;
    }

    // Minden hitelesitett felhasznalonak pontosan egy, azonos jogosultsaga van - nincs
    // admin/user megkulonbozteles ebben a projektben, a finomabb jogosultsag-ellenorzest
    // (pl. "csak a szervezo fogadhatja el a jelentkezest") a service reteg vegzi kezzel.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // A UserDetails interface elvarja ezt a nevet - itt a BCrypt-tel hash-elt jelszot adja
    // vissza, amit a DaoAuthenticationProvider hasonlit ossze a bejelentkezeskor kapott
    // nyers jelszo hash-evel.
    @Override
    public String getPassword() {
        return passwordHash;
    }

    // A UserDetails "felhasznalonev" fogalma - ebben a projektben az email cim tolti be
    // ezt a szerepet (nincs kulon username mezo).
    @Override
    public String getUsername() {
        return email;
    }
}
