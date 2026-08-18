package com.botlobby.backend.service;

import com.botlobby.backend.dto.rating.RatingRequest;
import com.botlobby.backend.dto.rating.RatingResponse;
import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.Rating;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.entity.enums.ParticipantStatus;
import com.botlobby.backend.exception.ConflictException;
import com.botlobby.backend.exception.ResourceNotFoundException;
import com.botlobby.backend.repository.LobbyParticipantRepository;
import com.botlobby.backend.repository.LobbyRepository;
import com.botlobby.backend.repository.RatingRepository;
import com.botlobby.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final LobbyRepository lobbyRepository;
    private final UserRepository userRepository;
    private final LobbyParticipantRepository lobbyParticipantRepository;
    private final RatingRepository ratingRepository;

    @Transactional
    public RatingResponse rate(UUID lobbyId, UUID raterId, RatingRequest request) {
        // Sorrendben, "elore kilepo" (fail-fast) ellenorzesek - a legolcsobb/legevidensebb
        // szabalyokat eloszor nezzuk meg, mielott dragabb adatbazis-lekerdezesekbe kezdenenk.
        if (raterId.equals(request.ratedUserId())) {
            throw new ConflictException("Onmagadat nem ertekelheted.");
        }

        Lobby lobby = findLobbyOrThrow(lobbyId);
        User rater = findUserOrThrow(raterId);
        User ratedUser = findUserOrThrow(request.ratedUserId());

        // Csak MULTBELI (mar lezajlott) esemenyt lehet ertekelni - meg zajlo/jovobeli
        // lobbynal meg nincs mit "hogy ment" alapjan visszajelezni.
        if (lobby.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Csak a lobby idopontja utan lehet ertekelni.");
        }
        // Mind az ertekelonek, mind az ertekeltnek reszt kellett vennie a lobbyban (szervezokent
        // vagy elfogadott resztvevokent) - idegenek nem ertekelhetik egymast.
        requireWasInLobby(lobby, raterId);
        requireWasInLobby(lobby, request.ratedUserId());
        if (ratingRepository.existsByLobbyIdAndRaterIdAndRatedUserId(lobbyId, raterId, request.ratedUserId())) {
            throw new ConflictException("Ezt a felhasznalot mar ertekelted ebben a lobbyban.");
        }

        // A mozgoatlag kepletehez a MEGLEVO (uj rating elotti) ertekelesek szama kell.
        long existingCount = ratingRepository.countByRatedUserId(ratedUser.getId());

        Rating rating = Rating.builder()
                .lobby(lobby)
                .rater(rater)
                .ratedUser(ratedUser)
                .stars(request.stars())
                // Vedelem null lista ellen: ha a kliens nem kuldott tags mezot, ures listat
                // hasznalunk uj ArrayList-kent (masolatot keszitve, nem magat a bejovo listat
                // hasznalva - igy a Rating.tags mindig egy fuggetlen, mutable lista marad).
                .tags(request.tags() != null ? new ArrayList<>(request.tags()) : new ArrayList<>())
                .comment(request.comment())
                .noShow(request.noShow())
                .build();

        Rating saved = ratingRepository.save(rating);
        // Mellekhatas: az ertekelt felhasznalo reliabilityScore-jat is frissiti (in-memory
        // modositas a mar betoltott User entitason - a JPA "dirty checking" mechanizmusa
        // miatt kulon .save() hivas nelkul is UPDATE-et general a tranzakcio vegen).
        updateReliabilityScore(ratedUser, saved, existingCount);

        return RatingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsForUser(UUID userId) {
        findUserOrThrow(userId);
        return ratingRepository.findByRatedUserId(userId).stream()
                .map(RatingResponse::from)
                .toList();
    }

    // Mozgoatlag: a reliabilityScore az osszes kapott ertekeles atlaga, ahol noShow=true eseten
    // az adott ertekeles 0 csillagkent szamit (fuggetlenul a beadott stars ertektol).
    private void updateReliabilityScore(User ratedUser, Rating newRating, long existingCount) {
        // noShow eseten a beadott csillagszamtol fuggetlenul 0 pontot ér - igy egy nem-
        // megjelenes mindig lehuzza az atlagot, meg ha "udvariassagbol" 5 csillagot is adnanak ra.
        double effectiveStars = Boolean.TRUE.equals(newRating.getNoShow()) ? 0.0 : newRating.getStars();
        // Klasszikus "uj atlag = (regi osszeg + uj ertek) / (regi darabszam + 1)" keplet:
        // visszaszamoljuk az eddigi ATLAGBOL az eddigi OSSZEGET (currentTotal), hozzaadjuk az
        // uj ertekelest, majd elosztjuk az uj (megnovekedett) darabszammal.
        double currentTotal = ratedUser.getReliabilityScore() * existingCount;
        double updatedScore = (currentTotal + effectiveStars) / (existingCount + 1);
        // Ket tizedesjegyre kerekitve tarolodik (pl. 4.666... helyett 4.67).
        ratedUser.setReliabilityScore(Math.round(updatedScore * 100.0) / 100.0);
    }

    private void requireWasInLobby(Lobby lobby, UUID userId) {
        boolean isCreator = lobby.getCreator().getId().equals(userId);
        boolean wasAccepted = lobbyParticipantRepository.findByLobbyIdAndUserId(lobby.getId(), userId)
                .map(participant -> participant.getStatus() == ParticipantStatus.ACCEPTED)
                .orElse(false);

        if (!isCreator && !wasAccepted) {
            throw new ConflictException("Csak a lobby (elfogadott) reszvevoi kozott lehet ertekelni.");
        }
    }

    private Lobby findLobbyOrThrow(UUID lobbyId) {
        return lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen lobby."));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen felhasznalo."));
    }
}
