package com.botlobby.backend.controller;

import com.botlobby.backend.dto.rating.RatingRequest;
import com.botlobby.backend.dto.rating.RatingResponse;
import com.botlobby.backend.security.UserPrincipal;
import com.botlobby.backend.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Lasd AuthController.java a controller-minta magyarazataert.
// @RequestMapping("/api"): itt szandekosan a legaltalanosabb "/api" gyokeret hasznaljuk
// (nem "/api/ratings"-t), mert a ket vegpont KET KULONBOZO eroforras ala tartozik
// (POST egy lobby AL-eroforraskent, GET egy user AL-eroforraskent) - ezert a teljes
// utvonalat mindegyik metodus sajat @PostMapping/@GetMapping-je adja meg.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // POST /api/lobbies/{id}/ratings - egy adott lobbyhoz tartozo ertekeles beadasa.
    @PostMapping("/lobbies/{id}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponse rate(@AuthenticationPrincipal UserPrincipal principal,
                                @PathVariable UUID id,
                                @Valid @RequestBody RatingRequest request) {
        return ratingService.rate(id, principal.getId(), request);
    }

    // GET /api/users/{id}/ratings - egy felhasznalo osszes kapott ertekelese (a profiloldalon
    // jelenik meg, lasd UserProfilePage.jsx). Nincs @AuthenticationPrincipal, mert ez publikus
    // adat - barki (bejelentkezett felhasznalo) megnezheti barki masnak az ertekeleseit.
    @GetMapping("/users/{id}/ratings")
    public List<RatingResponse> getRatingsForUser(@PathVariable UUID id) {
        return ratingService.getRatingsForUser(id);
    }
}
