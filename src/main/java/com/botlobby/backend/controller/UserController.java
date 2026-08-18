package com.botlobby.backend.controller;

import com.botlobby.backend.dto.user.SportProfileRequest;
import com.botlobby.backend.dto.user.SportProfileUpsertResult;
import com.botlobby.backend.dto.user.UserResponse;
import com.botlobby.backend.dto.user.UserSportProfileResponse;
import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.dto.user.UserUpdateRequest;
import com.botlobby.backend.security.UserPrincipal;
import com.botlobby.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// Lasd AuthController.java a controller-minta magyarazataert.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me - a "sajat profilom" nezet (ProfilePage.jsx) - a TELJES UserResponse-t
    // adja vissza email-lel egyutt, hiszen a hivo mindig maga a bejelentkezett felhasznalo.
    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getById(principal.getId());
    }

    // PUT /api/users/me - profil szerkesztese (nev/varos/profilkep, lasd UserUpdateRequest.java
    // arrol, miert nincs itt email/jelszo mezo).
    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                  @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateProfile(principal.getId(), request);
    }

    // Publikus, mas felhasznalok szamara is lathato kivonat (nincs benne email) - pl. lobby resztvevo/szervezo profiljanak megnyitasahoz.
    @GetMapping("/{id}")
    public UserSummaryResponse getPublicProfile(@PathVariable UUID id) {
        return userService.getPublicProfile(id);
    }

    // POST /api/users/me/sport-profiles - sportprofil felvetele/frissitese ("upsert", lasd
    // UserService.upsertSportProfile). A HTTP statuszt DINAMIKUSAN kell eldonteni (201 uj
    // profilnal, 200 frissitesnel), ezert itt NEM egyszeru @ResponseStatus-t hasznalunk (ami
    // statikus lenne), hanem kezzel epitett ResponseEntity-t, a service altal visszaadott
    // "created" jelzo alapjan.
    @PostMapping("/me/sport-profiles")
    public ResponseEntity<UserSportProfileResponse> upsertSportProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SportProfileRequest request) {
        SportProfileUpsertResult result = userService.upsertSportProfile(principal.getId(), request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.profile());
    }
}
