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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/lobbies/{id}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponse rate(@AuthenticationPrincipal UserPrincipal principal,
                                @PathVariable UUID id,
                                @Valid @RequestBody RatingRequest request) {
        return ratingService.rate(id, principal.getId(), request);
    }

    @GetMapping("/users/{id}/ratings")
    public List<RatingResponse> getRatingsForUser(@PathVariable UUID id) {
        return ratingService.getRatingsForUser(id);
    }
}
