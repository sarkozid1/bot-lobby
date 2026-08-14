package com.botlobby.backend.controller;

import com.botlobby.backend.dto.lobby.LobbyCreateRequest;
import com.botlobby.backend.dto.lobby.LobbyParticipantResponse;
import com.botlobby.backend.dto.lobby.LobbyResponse;
import com.botlobby.backend.dto.lobby.LobbyUpdateRequest;
import com.botlobby.backend.entity.enums.SkillLevel;
import com.botlobby.backend.security.UserPrincipal;
import com.botlobby.backend.service.LobbyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lobbies")
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyService lobbyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                 @Valid @RequestBody LobbyCreateRequest request) {
        return lobbyService.create(principal.getId(), request);
    }

    @GetMapping
    public List<LobbyResponse> search(
            @RequestParam(required = false) UUID sport,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) SkillLevel skillLevel) {
        return lobbyService.search(sport, city, date, skillLevel);
    }

    @GetMapping("/{id}")
    public LobbyResponse getById(@PathVariable UUID id) {
        return lobbyService.getById(id);
    }

    @PutMapping("/{id}")
    public LobbyResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                 @PathVariable UUID id,
                                 @Valid @RequestBody LobbyUpdateRequest request) {
        return lobbyService.update(id, principal.getId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        lobbyService.cancel(id, principal.getId());
    }

    @GetMapping("/{id}/participants")
    public List<LobbyParticipantResponse> getParticipants(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable UUID id) {
        return lobbyService.getParticipants(id, principal.getId());
    }

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyParticipantResponse join(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return lobbyService.join(id, principal.getId());
    }

    @PostMapping("/{id}/participants/{userId}/accept")
    public LobbyParticipantResponse accept(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable UUID id,
                                            @PathVariable UUID userId) {
        return lobbyService.accept(id, userId, principal.getId());
    }

    @PostMapping("/{id}/participants/{userId}/decline")
    public LobbyParticipantResponse decline(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable UUID id,
                                             @PathVariable UUID userId) {
        return lobbyService.decline(id, userId, principal.getId());
    }

    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        lobbyService.leave(id, principal.getId());
    }
}
