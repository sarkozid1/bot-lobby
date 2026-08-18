package com.botlobby.backend.service;

import com.botlobby.backend.dto.lobby.LobbyCreateRequest;
import com.botlobby.backend.dto.lobby.LobbyParticipantResponse;
import com.botlobby.backend.dto.lobby.LobbyResponse;
import com.botlobby.backend.dto.lobby.LobbyUpdateRequest;
import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.LobbyParticipant;
import com.botlobby.backend.entity.Sport;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.entity.enums.LobbyStatus;
import com.botlobby.backend.entity.enums.ParticipantStatus;
import com.botlobby.backend.entity.enums.SkillLevel;
import com.botlobby.backend.exception.ConflictException;
import com.botlobby.backend.exception.ForbiddenOperationException;
import com.botlobby.backend.exception.ResourceNotFoundException;
import com.botlobby.backend.repository.LobbyParticipantRepository;
import com.botlobby.backend.repository.LobbyRepository;
import com.botlobby.backend.repository.SportRepository;
import com.botlobby.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Ez a legnagyobb es legosszetettebb service a projektben - ez fogja ossze a lobby teljes
// eletciklusat: letrehozas -> jelentkezesek (join/accept/decline) -> letszam-/allapotkezeles
// (recomputeStatus) -> lezaras/lemondas. Szinte minden metodus elotte ellenorzi a jogosultsagot
// (requireCreator/requireCreatorOrAcceptedParticipant) es az allapot-szabalyokat, mielott
// barmit is modositana - igy az adatbazis sose kerulhet ervenytelen allapotba egy REST hivason keresztul.
@Service
@RequiredArgsConstructor
public class LobbyService {

    private final LobbyRepository lobbyRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final LobbyParticipantRepository lobbyParticipantRepository;

    // Uj lobby letrehozasa - a "creator" mindig a hitelesitett felhasznalo (a controller adja
    // at a JWT-bol kiolvasott userId-t, sosem a kliens altal kuldott adatbol jon).
    @Transactional
    public LobbyResponse create(UUID creatorId, LobbyCreateRequest request) {
        User creator = findUserOrThrow(creatorId);
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen sport."));

        Lobby lobby = Lobby.builder()
                .sport(sport)
                .creator(creator)
                .title(request.title())
                .description(request.description())
                .city(request.city())
                .startTime(request.startTime())
                .maxParticipants(request.maxParticipants())
                .minSkillLevel(request.minSkillLevel())
                .build();

        Lobby saved = lobbyRepository.save(lobby);
        // Vadonatuj lobbynal nyilvanvaloan meg senki sincs elfogadva, ezert a
        // currentParticipants parameter itt egyszeruen 0 (nincs ertelme kulon lekerdezest
        // inditani a meg csak most mentett, ures reszvevo-listahoz).
        return LobbyResponse.from(saved, 0);
    }

    // GET /api/lobbies?sport=&city=&date=&skillLevel= mogotti rugalmas, tobb-szempontu keresés.
    @Transactional(readOnly = true)
    public List<LobbyResponse> search(UUID sportId, String city, LocalDate date, SkillLevel skillLevel) {
        // A frontend csak egy NAPOT (LocalDate) kuld datum-szuronek, de az adatbazisban
        // pontos idobelyeg (LocalDateTime) van - ezert a napot ket hatarra (a nap eleje /
        // a KOVETKEZO nap eleje) bontjuk, es a LobbyRepository.search(...) egy fel-nyilt
        // intervallummal (>= startOfDay AND < endOfDay) szuri az adott napi lobbykat.
        LocalDateTime startOfDay = date != null ? date.atStartOfDay() : null;
        LocalDateTime endOfDay = date != null ? date.plusDays(1).atStartOfDay() : null;

        // Az enum erteket sztringge alakitjuk at, mert a natic SQL lekerdezes (lasd
        // LobbyRepository.search) varchar-kent, nem Java enum-kent kapja meg a parametert.
        String skillLevelName = skillLevel != null ? skillLevel.name() : null;
        return lobbyRepository.search(sportId, city, startOfDay, endOfDay, skillLevelName).stream()
                // Minden talalt lobbyhoz KULON lekerdezzuk az aktualis elfogadott letszamot
                // (countAccepted) - ez N+1 lekerdezes-mintat eredmenyez (nem a leghatekonyabb
                // nagy listaknal, de erre a meretre bven eleg egyszeru es athatlathato marad).
                .map(lobby -> LobbyResponse.from(lobby, countAccepted(lobby.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public LobbyResponse getById(UUID lobbyId) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        return LobbyResponse.from(lobby, countAccepted(lobbyId));
    }

    // Csak a szervezo szerkesztheti a lobby adatait, es csak addig, amig nincs lemondva.
    @Transactional
    public LobbyResponse update(UUID lobbyId, UUID requesterId, LobbyUpdateRequest request) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        requireCreator(lobby, requesterId);

        if (lobby.getStatus() == LobbyStatus.CANCELLED) {
            throw new ConflictException("Lemondott lobby nem szerkesztheto.");
        }

        // Nem lehet a maxletszamot a MAR elfogadott letszam ala csokkenteni - ez elkerulhetetlen
        // ellentmondast okozna (tobb elfogadott ember lenne, mint amennyit a lobby megengedne).
        long accepted = countAccepted(lobbyId);
        if (request.maxParticipants() < accepted) {
            throw new ConflictException(
                    "A maxParticipants nem lehet kisebb, mint a jelenlegi elfogadott letszam (" + accepted + ")."
            );
        }

        lobby.setTitle(request.title());
        lobby.setDescription(request.description());
        lobby.setCity(request.city());
        lobby.setStartTime(request.startTime());
        lobby.setMaxParticipants(request.maxParticipants());
        lobby.setMinSkillLevel(request.minSkillLevel());
        // A maxParticipants valtozhatott, ezert ujra ki kell szamolni, hogy OPEN vagy FULL
        // allapotba kerul-e a lobby (lasd recomputeStatus lent).
        recomputeStatus(lobby, accepted);

        return LobbyResponse.from(lobby, accepted);
    }

    @Transactional
    public void cancel(UUID lobbyId, UUID requesterId) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        requireCreator(lobby, requesterId);

        // Idempotens "soft delete": mar lemondott lobbyn nincs tovabbi hatasa.
        if (lobby.getStatus() != LobbyStatus.CANCELLED) {
            lobby.setStatus(LobbyStatus.CANCELLED);
        }
    }

    // A szervezonek (es az elfogadott resztvevoknek) lathatniuk kell, ki jelentkezett -
    // ez nelkul az accept/decline vegpontok gyakorlatilag hasznalhatatlanok lennenek.
    @Transactional(readOnly = true)
    public List<LobbyParticipantResponse> getParticipants(UUID lobbyId, UUID requesterId) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        requireCreatorOrAcceptedParticipant(lobby, requesterId);

        return lobbyParticipantRepository.findByLobbyId(lobbyId).stream()
                .map(LobbyParticipantResponse::from)
                .toList();
    }

    // Jelentkezes egy lobbyra - a jelentkezes mindig PENDING allapotban indul, a szervezonek
    // kell azt kesobb elfogadnia (accept) vagy elutasitania (decline).
    @Transactional
    public LobbyParticipantResponse join(UUID lobbyId, UUID userId) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        User user = findUserOrThrow(userId);

        if (lobby.getCreator().getId().equals(userId)) {
            throw new ConflictException("A sajat lobbydra nem jelentkezhetsz.");
        }
        if (lobby.getStatus() != LobbyStatus.OPEN) {
            throw new ConflictException("A lobby jelenleg nem fogad jelentkezoket.");
        }
        if (lobbyParticipantRepository.existsByLobbyIdAndUserId(lobbyId, userId)) {
            throw new ConflictException("Mar jelentkeztel erre a lobbyra.");
        }

        LobbyParticipant participant = LobbyParticipant.builder()
                .lobby(lobby)
                .user(user)
                .status(ParticipantStatus.PENDING)
                .build();

        return LobbyParticipantResponse.from(lobbyParticipantRepository.save(participant));
    }

    // A szervezo elfogadja egy jelentkezo jelentkezeset - ekkor valik a jelentkezo tenyleges
    // (letszamba szamito) resztvevove.
    @Transactional
    public LobbyParticipantResponse accept(UUID lobbyId, UUID participantUserId, UUID requesterId) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        requireCreator(lobby, requesterId);

        if (lobby.getStatus() == LobbyStatus.CANCELLED) {
            throw new ConflictException("Lemondott lobbyban nem lehet jelentkezot elfogadni.");
        }

        LobbyParticipant participant = findParticipantOrThrow(lobbyId, participantUserId);
        if (participant.getStatus() != ParticipantStatus.PENDING) {
            throw new ConflictException("A jelentkezo mar nincs fuggoben (PENDING) allapotban.");
        }

        // A letszam-ellenorzest KOZVETLENUL az elfogadas elott vegezzuk, hogy elkeruljuk azt
        // az eset, hogy tobb egyideju elfogadassal tulcsordulna a lobby maximum letszama.
        long accepted = countAccepted(lobbyId);
        if (accepted >= lobby.getMaxParticipants()) {
            throw new ConflictException("A lobby mar megtelt.");
        }

        participant.setStatus(ParticipantStatus.ACCEPTED);
        // +1: a MOST elfogadott jelentkezo meg nincs benne az accepted valtozoban (az a
        // korabbi allapotot tukrozi), ezert a friss allapot kiszamitasahoz kezzel hozzaadjuk.
        recomputeStatus(lobby, accepted + 1);

        return LobbyParticipantResponse.from(participant);
    }

    @Transactional
    public LobbyParticipantResponse decline(UUID lobbyId, UUID participantUserId, UUID requesterId) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        requireCreator(lobby, requesterId);

        LobbyParticipant participant = findParticipantOrThrow(lobbyId, participantUserId);
        if (participant.getStatus() != ParticipantStatus.PENDING) {
            throw new ConflictException("A jelentkezo mar nincs fuggoben (PENDING) allapotban.");
        }

        // Elutasitasnal NEM kell recomputeStatus - egy PENDING -> DECLINED valtas sosem
        // erinti az ACCEPTED letszamot, tehat a lobby OPEN/FULL allapota sem valtozhat.
        participant.setStatus(ParticipantStatus.DECLINED);
        return LobbyParticipantResponse.from(participant);
    }

    // Egy korabban jelentkezett/elfogadott felhasznalo onkent kilep.
    @Transactional
    public void leave(UUID lobbyId, UUID userId) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        LobbyParticipant participant = findParticipantOrThrow(lobbyId, userId);

        if (participant.getStatus() != ParticipantStatus.PENDING && participant.getStatus() != ParticipantStatus.ACCEPTED) {
            throw new ConflictException("Mar nem vagy resztvevoje ennek a lobbynak.");
        }

        boolean wasAccepted = participant.getStatus() == ParticipantStatus.ACCEPTED;
        participant.setStatus(ParticipantStatus.LEFT);

        // Csak akkor kell ujraszamolni a lobby allapotat (OPEN/FULL), ha a kilepo koraban
        // TENYLEGESEN beleszamitott a letszamba (ACCEPTED volt) - egy meg PENDING jelentkezo
        // kilepese sose valtoztatja meg a lobby FULL/OPEN statuszat.
        if (wasAccepted) {
            recomputeStatus(lobby, countAccepted(lobbyId));
        }
    }

    // Kozponti allapot-szamitas: a lobby letszamfuggo statusza (OPEN <-> FULL) mindig ebbol
    // a metodusbol dol el, sose kezzel, kulon-kulon a hivo helyeken - igy egyetlen helyen
    // van a "mikor FULL egy lobby" szabaly.
    private void recomputeStatus(Lobby lobby, long acceptedCount) {
        // CANCELLED/CLOSED szandekosan lezart allapotok, a letszamvaltozas nem irja felul oket.
        if (lobby.getStatus() == LobbyStatus.CANCELLED || lobby.getStatus() == LobbyStatus.CLOSED) {
            return;
        }
        lobby.setStatus(acceptedCount >= lobby.getMaxParticipants() ? LobbyStatus.FULL : LobbyStatus.OPEN);
    }

    private long countAccepted(UUID lobbyId) {
        return lobbyParticipantRepository.countByLobbyIdAndStatus(lobbyId, ParticipantStatus.ACCEPTED);
    }

    private void requireCreator(Lobby lobby, UUID requesterId) {
        if (!lobby.getCreator().getId().equals(requesterId)) {
            throw new ForbiddenOperationException("Csak a lobby letrehozoja vegezheti ezt a muveletet.");
        }
    }

    private void requireCreatorOrAcceptedParticipant(Lobby lobby, UUID userId) {
        boolean isCreator = lobby.getCreator().getId().equals(userId);
        boolean isAcceptedParticipant = lobbyParticipantRepository.findByLobbyIdAndUserId(lobby.getId(), userId)
                .map(participant -> participant.getStatus() == ParticipantStatus.ACCEPTED)
                .orElse(false);

        if (!isCreator && !isAcceptedParticipant) {
            throw new ForbiddenOperationException("Csak a lobby szervezoje vagy elfogadott resztvevoi latjak a reszvevo listat.");
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

    private LobbyParticipant findParticipantOrThrow(UUID lobbyId, UUID userId) {
        return lobbyParticipantRepository.findByLobbyIdAndUserId(lobbyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen jelentkezo ebben a lobbyban."));
    }
}
