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

// Lasd AuthController.java a controller-minta magyarazataert.
// Ez a legtobb vegpontot tartalmazo controller - a teljes lobby-eletciklust lefedi
// (letrehozas, keresés, szerkesztes, lemondas, jelentkezes-kezeles). Minden logika a
// LobbyService-ben van, itt csak az URL-ek/HTTP metodusok/parameterek definialasa tortenik.
@RestController
@RequestMapping("/api/lobbies")
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyService lobbyService;

    // POST /api/lobbies - uj lobby letrehozasa. A "principal.getId()"-t adjuk at szervezokent,
    // SOSEM egy a kliens altal a request testben kuldott azonositot - ezt a LobbyCreateRequest
    // DTO szandekosan nem is tartalmazza (lasd LobbyCreateRequest.java).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                 @Valid @RequestBody LobbyCreateRequest request) {
        return lobbyService.create(principal.getId(), request);
    }

    // GET /api/lobbies?sport=&city=&date=&skillLevel= - a lobbyk listaja/szurese
    // (LobbiesPage.jsx mogott). Minden @RequestParam(required = false): opcionalis
    // lekerdezesi parameter, hianyzo eseten null-kent erkezik a service-be.
    @GetMapping
    public List<LobbyResponse> search(
            @RequestParam(required = false) UUID sport,
            @RequestParam(required = false) String city,
            // @DateTimeFormat: megmondja Springnek, hogyan ertelmezze a query stringben
            // erkezo szoveges datumot (pl. "2026-09-10") LocalDate objektumma.
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) SkillLevel skillLevel) {
        return lobbyService.search(sport, city, date, skillLevel);
    }

    // GET /api/lobbies/{id} - egy konkret lobby reszletei. Nincs jogosultsag-ellenorzes itt:
    // az alap lobby-adatokat barki (bejelentkezett felhasznalo) lathatja, csak a
    // reszvevo-lista (lasd getParticipants lent) van korlatozva.
    @GetMapping("/{id}")
    public LobbyResponse getById(@PathVariable UUID id) {
        return lobbyService.getById(id);
    }

    // PUT /api/lobbies/{id} - lobby szerkesztese (csak a szervezo teheti, lasd LobbyService.update).
    @PutMapping("/{id}")
    public LobbyResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                 @PathVariable UUID id,
                                 @Valid @RequestBody LobbyUpdateRequest request) {
        return lobbyService.update(id, principal.getId(), request);
    }

    // DELETE /api/lobbies/{id} - valojaban NEM torli az adatbazis-sort, csak CANCELLED-re
    // allitja a statuszat (lasd LobbyService.cancel "soft delete" megjegyzeset). A HTTP DELETE
    // ige itt inkabb "eltavolitas a felhasznaloi elmenybol" ertelemben hasznalt.
    // 204 No Content: a muvelet sikeres, de nincs visszaadando tartalom.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        lobbyService.cancel(id, principal.getId());
    }

    // GET /api/lobbies/{id}/participants - a reszvevo-lista (csak a szervezo es az elfogadott
    // tagok lathatjak, lasd LobbyService.requireCreatorOrAcceptedParticipant).
    @GetMapping("/{id}/participants")
    public List<LobbyParticipantResponse> getParticipants(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable UUID id) {
        return lobbyService.getParticipants(id, principal.getId());
    }

    // POST /api/lobbies/{id}/join - jelentkezes egy lobbyra (kezdetben PENDING allapotba kerul).
    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyParticipantResponse join(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return lobbyService.join(id, principal.getId());
    }

    // POST /api/lobbies/{id}/participants/{userId}/accept - a szervezo elfogadja "userId"
    // jelentkezeset. Ket kulon azonosito szerepel az URL-ben: {id} a lobby, {userId} a
    // jelentkezo - a principal (aki a hivast inditja) egy HARMADIK szerepet tolt be:
    // "kell hogy a szervezo legyen" (ezt a service ellenorzi).
    @PostMapping("/{id}/participants/{userId}/accept")
    public LobbyParticipantResponse accept(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable UUID id,
                                            @PathVariable UUID userId) {
        return lobbyService.accept(id, userId, principal.getId());
    }

    // POST /api/lobbies/{id}/participants/{userId}/decline - lasd accept(...) fenti megjegyzeset.
    @PostMapping("/{id}/participants/{userId}/decline")
    public LobbyParticipantResponse decline(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable UUID id,
                                             @PathVariable UUID userId) {
        return lobbyService.decline(id, userId, principal.getId());
    }

    // POST /api/lobbies/{id}/leave - a bejelentkezett felhasznalo onkent kilep a lobbybol.
    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        lobbyService.leave(id, principal.getId());
    }
}
